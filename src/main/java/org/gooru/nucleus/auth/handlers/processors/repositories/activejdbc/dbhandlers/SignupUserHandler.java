package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import java.util.UUID;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.EmailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.emails.EmailNotificationBuilder;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
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
 * @author szgooru Created On: 02-Jan-2017
 *
 */
public class SignupUserHandler implements DBHandler {

    private final RedisClient redisClient;
    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(SignupUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private AJEntityUsers user;
    private AJEntityTenant tenant;

    public SignupUserHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {

        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            AJEntityUsers.signupFieldSelector(), AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        String tenantId = context.requestBody().getString(ParameterConstants.PARAM_TENANT_ID);
        String email = context.requestBody().getString(AJEntityUsers.EMAIL).toLowerCase();
        String username = context.requestBody().getString(AJEntityUsers.USERNAME).toLowerCase();

        LazyList<AJEntityUsers> users =
            AJEntityUsers.findBySQL(AJEntityUsers.SELECT_FOR_SIGNUP, email, username, tenantId);

        if (!users.isEmpty()) {
            LOGGER.error("user already exists with username: '{}' OR email: '{}'", username, email);
            return new ExecutionResult<>(MessageResponseFactory.createConflictRespose(), ExecutionStatus.FAILED);
        }

        tenant = AJEntityTenant.findById(UUID.fromString(tenantId));
        if (tenant == null) {
            LOGGER.warn("no tenant found for id: {}", tenantId);
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("tenant.not.found")),
                ExecutionStatus.FAILED);
        }

        user = new AJEntityUsers();
        user.setString(AJEntityUsers.LOGIN_TYPE, HelperConstants.UserLoginType.credential.getType());
        autoPopulate();

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        if (user.insert()) {
            final JsonObject result = new JsonObject();
            result.put(ParameterConstants.PARAM_USER_ID, user.getString(AJEntityUsers.ID));
            result.put(AJEntityUsers.USERNAME, user.getString(AJEntityUsers.USERNAME));
            result.put(ParameterConstants.PARAM_APP_ID,
                context.requestBody().getString(ParameterConstants.PARAM_APP_ID, null));
            result.put(ParameterConstants.PARAM_PARTNER_ID,
                context.requestBody().getString(ParameterConstants.PARAM_PARTNER_ID, null));
            result.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
            result.put(AJEntityUsers.EMAIL, user.getString(AJEntityUsers.EMAIL));
            result.put(ParameterConstants.PARAM_CDN_URLS, new JsonObject(tenant.getString(AJEntityTenant.CDN_URLS)));

            JsonObject tenantJson = new JsonObject();
            tenantJson.put(AJEntityUsers.TENANT_ID, tenant.getString(AJEntityTenant.ID));
            tenantJson.put(AJEntityUsers.TENANT_ROOT, user.getString(AJEntityUsers.TENANT_ROOT));
            result.put(ParameterConstants.PARAM_TENANT, tenantJson);

            // Check if there is no validity and set to default;
            int accessTokenValidity = tenant.getInteger(AJEntityTenant.ACCESS_TOKEN_VALIDITY);
            final String token = InternalHelper.generateToken(user.getString(AJEntityUsers.ID), null,
                tenant.getString(AJEntityTenant.ID));
            saveAccessToken(token, result, accessTokenValidity);

            result.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
            result.put(AJEntityUsers.FIRST_NAME, user.getString(AJEntityUsers.FIRST_NAME));
            result.put(AJEntityUsers.LAST_NAME, user.getString(AJEntityUsers.LAST_NAME));
            result.put(AJEntityUsers.USER_CATEGORY, user.getString(AJEntityUsers.USER_CATEGORY));
            result.put(AJEntityUsers.THUMBNAIL, user.getString(AJEntityUsers.THUMBNAIL));

            EmailNotificationBuilder emailNotificationBuilder = new EmailNotificationBuilder();
            emailNotificationBuilder.setTemplateName(EmailTemplateConstants.WELCOME_MAIL).addToAddress(user.getString(AJEntityUsers.EMAIL));
            
            LOGGER.info("user created successfully");
            return new ExecutionResult<>(
                MessageResponseFactory.createPostResponse(result,
                    EventBuilderFactory.getSignupUserEventBuilder(user.getString(AJEntityUsers.ID), emailNotificationBuilder)),
                ExecutionStatus.SUCCESSFUL);
        }

        LOGGER.debug("unable to create new user");
        return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Unable to create user"),
            ExecutionStatus.FAILED);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private void saveAccessToken(String token, JsonObject session, Integer expireAtInSeconds) {
        session.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, session.toString(), expireAtInSeconds);
    }

    private void autoPopulate() {
        new DefaultAJEntityUsersBuilder().build(user, context.requestBody(), AJEntityUsers.getConverterRegistry());
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityUsersBuilder implements EntityBuilder<AJEntityUsers> {
    }

}
