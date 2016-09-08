package org.gooru.nucleus.auth.handlers.processors.command.executor.internal;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;

import java.util.Arrays;
import java.util.List;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

import io.vertx.core.json.JsonObject;

public class AuthenticateExecutor implements DBExecutor {

	private final MessageContext messageContext;
	private String basicAuthCredentials;
	private AJEntityUserIdentity userIdentity;
	private AuthClientDTO authClientDTO;
	private AJEntityAuthClient authClient;
	private static List<String> USER_DEMOGRAPHIC_FIELDS = Arrays.asList("firstname", "lastname", "user_category",
			"birth_date", "grade", "course", "thumbnail_path", "gender", "about_me", "school_id", "school",
			"school_district_id", "school_district", "email_id", "country_id", "country", "state_id", "state",
			"metadata", "roster_id", "roster_global_userid");
     

	 public AuthenticateExecutor(MessageContext messageContext) {
		this.messageContext = messageContext;
	}

	@Override
	public void checkSanity() {
		basicAuthCredentials = messageContext.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
		authClientDTO = new AuthClientDTO(messageContext.requestBody());
        reject(!(GrantType.CREDENTIAL.getType().equalsIgnoreCase(authClientDTO.getGrantType())),
            MessageCodeConstants.AU0003, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
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

		final String credentials[] = InternalHelper.getUsernameAndPassword(basicAuthCredentials);
		final String username = credentials[0];
		final String password = InternalHelper.encryptPassword(credentials[1]);

		if (username.indexOf("@") > 1) {
			LazyList<AJEntityUserIdentity> userIdentityEmail = AJEntityUserIdentity
					.where(AJEntityUserIdentity.GET_BY_EMAIL_PASSWORD, username, password);
			userIdentity = userIdentityEmail.size() > 0 ? userIdentityEmail.get(0) : null;
		} else {
			LazyList<AJEntityUserIdentity> userIdentityUsername = AJEntityUserIdentity
					.where(AJEntityUserIdentity.GET_BY_CANONICAL_USERNAME_PASSWORD, username.toLowerCase(), password);
			userIdentity = userIdentityUsername.size() > 0 ? userIdentityUsername.get(0) : null;
		}
		rejectIfNull(userIdentity, MessageCodeConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
		reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED),
				MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
	}

	@Override
	public MessageResponse executeRequest() {
        LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userIdentity.getUserId());
        AJEntityUser user = users.size() > 0 ? users.get(0) : null;
        JsonObject response = new JsonObject(JsonFormatterBuilder.buildSimpleJsonFormatter(false, USER_DEMOGRAPHIC_FIELDS).toJson(user));
        
		response.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
		response.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        response.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        final String token = InternalHelper.generateToken(authClient.getClientId(), userIdentity.getUserId());
        response.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
		return new MessageResponse.Builder().setResponseBody(response)
	            .setContentTypeJson().setStatusOkay().successful().build();
	}

	@Override
	public boolean handlerReadOnly() {
		return true;
	}

}
