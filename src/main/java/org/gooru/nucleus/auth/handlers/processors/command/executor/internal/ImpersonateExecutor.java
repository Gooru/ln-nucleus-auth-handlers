package org.gooru.nucleus.auth.handlers.processors.command.executor.internal;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class ImpersonateExecutor implements DBExecutor {

	private RedisClient redisClient;
	private final MessageContext messageContext;
	private String basicAuthCredentials;
	private AJEntityUserIdentity userIdentity;
	private AuthClientDTO authClientDTO;
	private AJEntityAuthClient authClient;
	private static Logger LOGGER = LoggerFactory.getLogger(ImpersonateExecutor.class);
    private static List<String> ALLOWED_GRANT_TYPES =
        Arrays.asList(GrantType.CREDENTIAL.getType(), GrantType.GOOGLE.getType(), GrantType.ANONYMOUS.getType(),
            GrantType.SAML.getType(), GrantType.WSFED.getType());
	
	public ImpersonateExecutor(MessageContext messageContext) {
		this.messageContext = messageContext;
		this.redisClient = RedisClient.instance();
	}

	@Override
	public void checkSanity() {
		basicAuthCredentials = messageContext.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
		authClientDTO = new AuthClientDTO(messageContext.requestBody());
        reject(!(ALLOWED_GRANT_TYPES.contains(authClientDTO.getGrantType())), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
		rejectIfNullOrEmpty(basicAuthCredentials, MessageCodeConstants.AU0006,
	            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

	}

	@Override
	public void validateRequest() {
		LazyList<AJEntityAuthClient> authClients = AJEntityAuthClient.where(
				AJEntityAuthClient.GET_AUTH_CLIENT_ID_AND_KEY, authClientDTO.getClientId(),
				InternalHelper.encryptClientKey(authClientDTO.getClientKey()));
		authClient = authClients.size() > 0 ? authClients.get(0) : null;
		rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
		reject((authClient.getGrantTypes() == null
				|| !authClient.getGrantTypes().contains(authClientDTO.getGrantType())), MessageCodeConstants.AU0005,
				HttpConstants.HttpStatus.FORBIDDEN.getCode());

		byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
		final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
		final String[] credentials = credential.split(":");
		String userId = credentials[0];
		LOGGER.debug("userid: {}", userId);

		LazyList<AJEntityUserIdentity> userIdentityByUserId = AJEntityUserIdentity
				.where(AJEntityUserIdentity.GET_BY_USER_ID, userId);
		userIdentity = userIdentityByUserId.size() > 0 ? userIdentityByUserId.get(0) : null;
		rejectIfNull(userIdentity, MessageCodeConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
		reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED),
				MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
	}

	@Override
	public MessageResponse executeRequest() {
        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        final String token = InternalHelper.generateToken(authClient.getClientId(), userIdentity.getUserId());
        JsonObject prefs = new JsonObject();
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userIdentity.getUserId());
        AJEntityUser user = users.size() > 0 ? users.get(0) : null;
        if (user.getFirstname() != null) {
            accessToken.put(ParameterConstants.PARAM_USER_FIRSTNAME, user.getFirstname());
        }
        if (user.getLastname() != null) {
            accessToken.put(ParameterConstants.PARAM_USER_LASTNAME, user.getLastname());
        }
        if (user.getUserCategory() != null) {
            accessToken.put(ParameterConstants.PARAM_USER_CATEGORY, user.getUserCategory());
        }
        
        return new MessageResponse.Builder().setResponseBody(accessToken)
            .setContentTypeJson().setStatusOkay().successful().build();
	}

	@Override
	public boolean handlerReadOnly() {
		return true;
	}
	
	private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }

}
