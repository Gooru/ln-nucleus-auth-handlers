package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonObject;

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
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

public final class CreateAnonymousAccessTokenExecutor implements DBExecutor {

    private RedisClient redisClient;
    private final MessageContext messageContext;
    private AJEntityAuthClient authClient;
    private AuthClientDTO authClientDTO;

    public CreateAnonymousAccessTokenExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        authClientDTO = new AuthClientDTO(messageContext.requestBody());
        reject(!(GrantType.ANONYMOUS.getType().equalsIgnoreCase(authClientDTO.getGrantType())),
            MessageCodeConstants.AU0003, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

    }

    @Override
    public void validateRequest() {
        String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
        rejectIfNullOrEmpty(authClientDTO.getClientId(), MessageCodeConstants.AU0001,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        rejectIfNullOrEmpty(authClientDTO.getClientKey(), MessageCodeConstants.AU0002,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        LazyList<AJEntityAuthClient> authClients =
            AJEntityAuthClient.where(AJEntityAuthClient.GET_AUTH_CLIENT_ID_AND_KEY, authClientDTO.getClientId(),
                InternalHelper.encryptClientKey(authClientDTO.getClientKey()));
        authClient = authClients.size() > 0 ? authClients.get(0) : null;
        rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        reject((authClient.getGrantTypes() == null || !authClient.getGrantTypes()
            .contains(authClientDTO.getGrantType())), MessageCodeConstants.AU0005,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
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

    }

    @Override
    public MessageResponse executeRequest() {
        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        
        //Temporary changes to support multitenancy
        JsonObject tenantJson = new JsonObject();
        String strNull = null;
        tenantJson.put(ParameterConstants.PARAM_TENANT_ID, "ba956a97-ae15-11e5-a302-f8a963065976");
        tenantJson.put(ParameterConstants.PARAM_TENANT_ROOT, strNull);
        accessToken.put(ParameterConstants.PARAM_TENANT, tenantJson);
        
        JsonObject prefs = new JsonObject();
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, "");
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        final String token =
            InternalHelper.generateToken(authClient.getClientId(), MessageConstants.MSG_USER_ANONYMOUS);
        saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay()
            .successful().build();
    }

    private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
