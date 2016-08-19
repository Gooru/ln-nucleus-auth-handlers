package org.gooru.nucleus.auth.handlers.processors.command.executor.authorize;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonObject;

import java.util.Random;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthorizeDTO;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

class AuthorizeUserExecutor implements DBExecutor {

    private RedisClient redisClient;
    private AJEntityAuthClient authClient;
    private final MessageContext messageContext;
    private AuthorizeDTO authorizeDTO;

    public AuthorizeUserExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        authorizeDTO = new AuthorizeDTO(messageContext.requestBody());
        reject((HelperConstants.SSO_CONNECT_GRANT_TYPES.get(authorizeDTO.getGrantType()) == null),
            MessageCodeConstants.AU0003, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

    }

    @Override
    public void validateRequest() {
        String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
        rejectIfNullOrEmpty(authorizeDTO.getClientId(), MessageCodeConstants.AU0001,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        rejectIfNullOrEmpty(authorizeDTO.getClientKey(), MessageCodeConstants.AU0002,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        LazyList<AJEntityAuthClient> results =
            AJEntityAuthClient.where(AJEntityAuthClient.GET_AUTH_CLIENT_ID_AND_KEY, authorizeDTO.getClientId(),
                InternalHelper.encryptClientKey(authorizeDTO.getClientKey()));
        authClient = results.size() > 0 ? results.get(0) : null;
        rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        reject(
            (authClient.getGrantTypes() == null || !authClient.getGrantTypes().contains(authorizeDTO.getGrantType())),
            MessageCodeConstants.AU0005, HttpConstants.HttpStatus.FORBIDDEN.getCode());

        if (requestDomain != null && authClient.getRefererDomains() != null) {
            boolean isValidReferrer = false;
            for (Object whitelistedDomain : authClient.getRefererDomains()) {
                if (requestDomain.endsWith(((String) whitelistedDomain))) {
                    isValidReferrer = true;
                    break;
                }
            }
            reject(!isValidReferrer, MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
        }

        reject(authorizeDTO.getUser() == null, MessageCodeConstants.AU0038, 400);
        JsonObject errors = new JsonObject();
        addValidator(errors, authorizeDTO.getUser().getIdentityId() == null,
            ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID, MessageCodeConstants.AU0033);
        rejectError(errors, HttpConstants.HttpStatus.BAD_REQUEST.getCode());

    }

    @Override
    public MessageResponse executeRequest() {
        String identityId = authorizeDTO.getUser().getIdentityId();
        boolean isEmailIdentity = false;
        AJEntityUserIdentity userIdentity = null;
        AJEntityUser user = null;
        EventBuilder eventBuilder = new EventBuilder();
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
        if (identityId.indexOf("@") > 1) {
            isEmailIdentity = true;
            LazyList<AJEntityUserIdentity> userIdentityEmail =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, identityId);
            userIdentity = userIdentityEmail.size() > 0 ? userIdentityEmail.get(0) : null;
        } else {
            LazyList<AJEntityUserIdentity> userIdentityReference =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_REFERENCE, identityId);
            userIdentity = userIdentityReference.size() > 0 ? userIdentityReference.get(0) : null;
        }
        if (userIdentity == null) {
            ActionResponseDTO<AJEntityUserIdentity> responseDTO =
                createUserWithIdentity(authorizeDTO.getUser(), authorizeDTO.getGrantType(), authorizeDTO.getClientId(),
                    isEmailIdentity, eventBuilder);
            userIdentity = responseDTO.getModel();
            eventBuilder = responseDTO.getEventBuilder();
            mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).addToAddress(
                userIdentity.getEmailId());
        } else {
            LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userIdentity.getUserId());
            user = users.size() > 0 ? users.get(0) : null;
        }

        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        final String token = InternalHelper.generateToken(authClient.getClientId(), userIdentity.getUserId());
        JsonObject prefs = new JsonObject();
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        
        if ( user != null ) {
           accessToken.put(ParameterConstants.PARAM_USER_THUMBNAIL_PATH, user.getThumbnailPath());
        }
        
        eventBuilder.setEventName(Event.AUTHORIZE_USER.getName())
            .putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
            .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
            .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
            .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, authorizeDTO.getGrantType());
        return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .addMailNotify(mailNotifyBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();

    }

    private ActionResponseDTO<AJEntityUserIdentity> createUserWithIdentity(final UserDTO userDTO,
        final String grantType, final String clientId, final boolean isEmailIdentity, final EventBuilder eventBuilder) {
        final AJEntityUser user = new AJEntityUser();
        user.setFirstname(userDTO.getFirstname());
        if (userDTO.getLastname() != null) {
            user.setLastname(userDTO.getLastname());
        }
        user.saveIt();
        eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, HelperConstants.USERS_JSON_FIELDS).toJson(user));

        final AJEntityUserIdentity userIdentity = createUserIdentityValue(grantType, user, clientId);
        if (isEmailIdentity) {
            userIdentity.setEmailId(userDTO.getIdentityId());
            userIdentity.setEmailConfirmStatus(true);
        } else {
            userIdentity.setReferenceId(userDTO.getIdentityId());
        }
        if (userDTO.getUsername() == null) {
            StringBuilder username = new StringBuilder(userDTO.getFirstname().replaceAll("\\s+", ""));
            if (userDTO.getLastname() != null && userDTO.getLastname().length() > 0 && username.length() < 14) {
                final String lastname = userDTO.getLastname();
                username.append(lastname.substring(0, lastname.length() > 5 ? 5 : lastname.length()));
            }
            LazyList<AJEntityUserIdentity> results =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_CANONICAL_USERNAME, username.toString().toLowerCase());
            AJEntityUserIdentity identityUsername = results.size() > 0 ? results.get(0) : null;
            if (identityUsername != null) {
                final Random randomNumber = new Random();
                username.append(randomNumber.nextInt(1000));
            }
            userIdentity.setUsername(username.toString());
            userIdentity.setCanonicalUsername(username.toString().toLowerCase());
        } else {
            userIdentity.setUsername(userDTO.getUsername());
            userIdentity.setCanonicalUsername(userDTO.getUsername().toLowerCase());
        }
        userIdentity.saveIt();
        eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).toJson(userIdentity));
        return new ActionResponseDTO<>(userIdentity, eventBuilder);
    }

    private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }

    private AJEntityUserIdentity createUserIdentityValue(final String userIdentityAuthorizeType,
        final AJEntityUser user, final String clientId) {
        final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
        userIdentity.setUserId(user.getId());
        userIdentity.setLoginType(userIdentityAuthorizeType);
        userIdentity.setProvisionType(userIdentityAuthorizeType);
        userIdentity.setClientId(clientId);
        userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
        return userIdentity;
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
