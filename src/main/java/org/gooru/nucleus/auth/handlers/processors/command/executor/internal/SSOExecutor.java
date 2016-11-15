package org.gooru.nucleus.auth.handlers.processors.command.executor.internal;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.LTISSODTO;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class SSOExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private RedisClient redisClient;
    private LTISSODTO ltissodto;
    private String basicAuthCredentials;
    private AJEntityAuthClient authClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(SSOExecutor.class);
    
    public SSOExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public void checkSanity() {

        basicAuthCredentials = messageContext.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
        rejectIfNullOrEmpty(basicAuthCredentials, MessageCodeConstants.AU0006,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

        ltissodto = new LTISSODTO(messageContext.requestBody());

        // Reject if there are no user details present in the request
        reject(ltissodto.getUser() == null, MessageCodeConstants.AU0038,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode());

        // Reject if reference_id is not present in request
        rejectIfNullOrEmpty(ltissodto.getUser().getReferenceId(), MessageCodeConstants.AU0053,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode());

        String userCategory = ltissodto.getUser().getUserCategory();
        if (userCategory != null && HelperConstants.USER_CATEGORY.get(userCategory) == null) {
            reject(true, MessageCodeConstants.AU0025, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
        }

        // Client Id and key of caller (actual request of SSO launch) must be
        // present
        rejectIfNullOrEmpty(ltissodto.getClientId(), MessageCodeConstants.AU0001,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        rejectIfNullOrEmpty(ltissodto.getClientKey(), MessageCodeConstants.AU0002,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        LOGGER.debug("checkSanity: OK");
    }

    @Override
    public void validateRequest() {

        final String credentials[] = InternalHelper.getClientIdAndSecret(basicAuthCredentials);
        final String clientId = credentials[0];
        final String clientSecret = credentials[1];

        Long authCount = Base.count(AJEntityAuthClient.TABLE, AJEntityAuthClient.GET_AUTH_CLIENT_ID_AND_KEY, clientId,
            InternalHelper.encryptClientKey(clientSecret));
        reject(authCount == 0, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

        LazyList<AJEntityAuthClient> authClientResult =
            AJEntityAuthClient.where(AJEntityAuthClient.VERIFY_CLIENT, ltissodto.getClientId(),
                InternalHelper.encryptClientKey(ltissodto.getClientKey()), HelperConstants.GRANT_TYPE_SSO);
        authClient = authClientResult.size() > 0 ? authClientResult.get(0) : null;
        rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        LOGGER.debug("validateRequest: OK");
    }

    @Override
    public MessageResponse executeRequest() {
        String referenceId = ltissodto.getUser().getReferenceId();
        String clientId = ltissodto.getClientId();
        EventBuilder eventBuilder = new EventBuilder();
        AJEntityUserIdentity userIdentity = null;
        AJEntityUser user = null;

        LazyList<AJEntityUserIdentity> userIdentityReference =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_REFERENCE_AND_CLIENTID, referenceId, clientId);
        userIdentity = userIdentityReference.size() > 0 ? userIdentityReference.get(0) : null;

        if (userIdentity == null) {
            LOGGER.info("No user identity present for reference_id '{}' and client_id '{}'. Creating new", referenceId, clientId);
            ActionResponseDTO<AJEntityUserIdentity> responseDTO =
                createUserWithIdentity(ltissodto.getUser(), clientId, eventBuilder);
            userIdentity = responseDTO.getModel();
            eventBuilder = responseDTO.getEventBuilder();
        } else {
            LOGGER.info("user identity already exists for reference_id '{}' and client_id '{}'", referenceId, clientId);
            LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userIdentity.getUserId());
            user = users.size() > 0 ? users.get(0) : null;
        }

        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        accessToken.put(ParameterConstants.PARAM_USER_REFERENCE_ID, userIdentity.getReferenceId());
        accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, clientId);
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        final String token = InternalHelper.generateToken(clientId, userIdentity.getUserId());
        JsonObject prefs = new JsonObject();
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);

        if (user != null) {
            accessToken.put(ParameterConstants.PARAM_USER_THUMBNAIL_PATH, user.getThumbnailPath());
        }

        eventBuilder.setEventName(Event.LTISSO_LAUNCH.getName())
            .putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
            .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
            .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
            .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, userIdentity.getLoginType());
        return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .setContentTypeJson().setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private ActionResponseDTO<AJEntityUserIdentity> createUserWithIdentity(final UserDTO userDTO, final String clientId,
        final EventBuilder eventBuilder) {
        final AJEntityUser user = new AJEntityUser();
        user.setFirstname(userDTO.getFirstname());
        if (userDTO.getLastname() != null) {
            user.setLastname(userDTO.getLastname());
        }

        if (userDTO.getEmailId() != null) {
            user.setEmailId(userDTO.getEmailId());
        }

        if (userDTO.getUserCategory() != null) {
            user.setUserCategory(userDTO.getUserCategory());
        }

        user.saveIt();
        eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, HelperConstants.USERS_JSON_FIELDS).toJson(user));

        final AJEntityUserIdentity userIdentity = createUserIdentityValue(user, clientId);
        userIdentity.setReferenceId(userDTO.getReferenceId());
        if (userDTO.getUsername() != null) {
            userIdentity.setUsername(userDTO.getUsername());
            userIdentity.setCanonicalUsername(userDTO.getUsername().toLowerCase());
        }
        
        if (userDTO.getEmailId() != null) {
            userIdentity.setEmailId(userDTO.getEmailId().toLowerCase());
            userIdentity.setEmailConfirmStatus(true);
        }

        userIdentity.saveIt();
        eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).toJson(userIdentity));
        return new ActionResponseDTO<>(userIdentity, eventBuilder);
    }

    private AJEntityUserIdentity createUserIdentityValue(final AJEntityUser user, final String clientId) {
        final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
        userIdentity.setUserId(user.getId());
        userIdentity.setLoginType(AJEntityUserIdentity.LOGIN_TYPE_LTISSO);
        userIdentity.setProvisionType(AJEntityUserIdentity.PROVISION_TYPE_LTISSO);
        userIdentity.setClientId(clientId);
        userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
        return userIdentity;
    }

    private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }
}
