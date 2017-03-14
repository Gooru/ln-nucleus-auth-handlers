package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import java.util.UUID;

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
import org.gooru.nucleus.auth.handlers.processors.responses.ResoponseBuilder;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public class SignupUserHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(SignupUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private AJEntityUsers user;
    private AJEntityTenant tenant;

    public SignupUserHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {

        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.requestBody(), AJEntityUsers.signupFieldSelector(),
                AJEntityUsers.getValidatorRegistry());
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
            user = users.get(0);
            String usernameFromDB = user.getString(AJEntityUsers.USERNAME);
            String emailFromDB = user.getString(AJEntityUsers.EMAIL);
            JsonObject errors = new JsonObject();
            if (usernameFromDB.equalsIgnoreCase(username)) {
                LOGGER.error("user already exists with username: '{}'", username);
                errors.put(AJEntityUsers.USERNAME, "'" + username + "'" + " is already taken");
            }

            if (emailFromDB.equalsIgnoreCase(email)) {
                LOGGER.error("user already exists with email: '{}'", email);
                errors.put(AJEntityUsers.EMAIL, "'" + email + "'" + " is already taken");
            }
            return new ExecutionResult<>(MessageResponseFactory.createConflictRespose(errors), ExecutionStatus.FAILED);
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
            final JsonObject result = new ResoponseBuilder(context, user, tenant, null).build();

            EmailNotificationBuilder emailNotificationBuilder = new EmailNotificationBuilder();
            emailNotificationBuilder.setTemplateName(EmailTemplateConstants.WELCOME_MAIL)
                .addToAddress(user.getString(AJEntityUsers.EMAIL));

            LOGGER.info("user created successfully");
            return new ExecutionResult<>(MessageResponseFactory.createPostResponse(result, EventBuilderFactory
                .getSignupUserEventBuilder(user.getString(AJEntityUsers.ID), emailNotificationBuilder)),
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

    private void autoPopulate() {
        new DefaultAJEntityUsersBuilder().build(user, context.requestBody(), AJEntityUsers.getConverterRegistry());
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityUsersBuilder implements EntityBuilder<AJEntityUsers> {
    }

}
