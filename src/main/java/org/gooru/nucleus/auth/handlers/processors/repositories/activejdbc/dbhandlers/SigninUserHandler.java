package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.RequestValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 03-Jan-2017
 *
 */
public class SigninUserHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(SigninUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private final RedisClient redisClient;
    private static String basicCredentials;
    private static String clientId;
    private static String clientKey;
    private static AJEntityPartner partner;
    private static AJEntityTenant tenant;
    private static AJEntityUsers user;

    public SigninUserHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            RequestValidator.authorizeFieldSelector(), RequestValidator.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        
        String grantType = context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE);
        if (!grantType.equalsIgnoreCase(HelperConstants.GrantTypes.credential   .getType())) {
            LOGGER.warn("missing or invalid grant type in request");
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse(RESOURCE_BUNDLE.getString("invalid.granttype")),
                ExecutionStatus.FAILED);
        }

        clientId = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_ID);
        clientKey = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_KEY);

        basicCredentials = context.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
        if (basicCredentials == null || basicCredentials.isEmpty()) {
            LOGGER.warn("invalid credentials in request");
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse(RESOURCE_BUNDLE.getString("invalid.credential")),
                ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityTenant> tenants;

        // First lookup in partner if not found, fall back on tenant
        LazyList<AJEntityPartner> partners = AJEntityPartner.findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, clientId,
            InternalHelper.encryptClientKey(clientKey));
        if (partners.isEmpty()) {
            tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, clientId,
                InternalHelper.encryptClientKey(clientKey), HelperConstants.GrantTypes.credential.getType());
        } else {
            partner = partners.get(0);
            tenants =
                AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID, partner.getString(AJEntityPartner.TENANT_ID));
        }

        if (tenants.isEmpty()) {
            LOGGER.warn("No matching partner or tenant found for client_id '{}' and client_key '{}'", clientId,
                clientKey);
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("tenant.not.found")),
                ExecutionStatus.FAILED);
        }

        tenant = tenants.get(0);

        final String credentials[] = InternalHelper.getUsernameAndPassword(basicCredentials);
        final String username = credentials[0];
        final String password = InternalHelper.encryptPassword(credentials[1]);

        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_FOR_SIGNIN, username, username,
            tenant.getString(AJEntityTenant.ID));
        if (users.isEmpty()) {
            LOGGER.warn("user not found in database for username/email: {}", username);
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        user = users.get(0);
        if (!password.equals(user.getString(AJEntityUsers.PASSWORD))) {
            LOGGER.warn("Invalid password provided while login");
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse((RESOURCE_BUNDLE.getString("invalid.password"))),
                ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        final JsonObject result = new JsonObject();
        result.put(ParameterConstants.PARAM_USER_ID, user.getString(AJEntityUsers.ID));
        result.put(ParameterConstants.PARAM_APP_ID,
            context.requestBody().getString(ParameterConstants.PARAM_APP_ID, null));
        result.put(ParameterConstants.PARAM_PARTNER_ID,
            (partner != null) ? partner.getString(AJEntityPartner.ID) : null);
        result.put(AJEntityUsers.USERNAME, user.getString(AJEntityUsers.USERNAME));
        result.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        result.put(AJEntityUsers.EMAIL, user.getString(AJEntityUsers.EMAIL));
        result.put(ParameterConstants.PARAM_CDN_URLS, new JsonObject(tenant.getString(AJEntityTenant.CDN_URLS)));

        JsonObject tenantJson = new JsonObject();
        tenantJson.put(AJEntityUsers.TENANT_ID, tenant.getString(AJEntityTenant.ID));
        tenantJson.put(AJEntityUsers.TENANT_ROOT, user.getString(AJEntityUsers.TENANT_ROOT));
        result.put(ParameterConstants.PARAM_TENANT, tenantJson);

        int accessTokenValidity = (partner != null) ? partner.getInteger(AJEntityPartner.ACCESS_TOKEN_VALIDITY)
            : tenant.getInteger(AJEntityTenant.ACCESS_TOKEN_VALIDITY);
        String partnerId = (partner != null) ? partner.getString(AJEntityPartner.ID) : null;
        final String token = InternalHelper.generateToken(user.getString(AJEntityUsers.ID), partnerId, clientId);
        saveAccessToken(token, result, accessTokenValidity);

        result.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        result.put(AJEntityUsers.FIRST_NAME, user.getString(AJEntityUsers.FIRST_NAME));
        result.put(AJEntityUsers.LAST_NAME, user.getString(AJEntityUsers.LAST_NAME));
        result.put(AJEntityUsers.USER_CATEGORY, user.getString(AJEntityUsers.USER_CATEGORY));
        result.put(AJEntityUsers.THUMBNAIL, user.getString(AJEntityUsers.THUMBNAIL));

        LOGGER.debug("user token generated successfully");
        return new ExecutionResult<>(
            MessageResponseFactory.createGetResponse(result,
                EventBuilderFactory.getSigninUserEventBuilder(user.getString(AJEntityUsers.ID))),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private void saveAccessToken(String token, JsonObject session, Integer expireAtInSeconds) {
        session.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, session.toString(), expireAtInSeconds);
    }
    
    private static class DefaultPayloadValidator implements PayloadValidator {
    }

}
