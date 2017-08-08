package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.EmailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.emails.EmailNotificationBuilder;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.DBHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 03-Jan-2017
 */
public class TriggerResetPasswordHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerResetPasswordHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private final RedisClient redisClient;
    private String email;
    private String tenantId;
    private String partnerId;
    private AJEntityUsers user;

    public TriggerResetPasswordHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.requestBody(), AJEntityUsers.triggerResetPasswordEmailFieldSelector(),
                AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        email = context.requestBody().getString(AJEntityUsers.EMAIL).toLowerCase();
        tenantId = context.requestBody().getString(AJEntityUsers.TENANT_ID);
        partnerId = context.requestBody().getString(AJEntityUsers.PARTNER_ID);
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        user = DBHelper.getUserByEmailAndTenantId(email, tenantId, partnerId);
        if (user == null) {
            LOGGER.warn("user not found in database for email: {} and Tenant: {}, Partner: {}", email, tenantId, partnerId);
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        final String token = InternalHelper.generatePasswordResetToken(user.getString(AJEntityUsers.ID));
        JsonObject redisPacket =
            new JsonObject().put(AJEntityUsers.EMAIL, email).put(AJEntityUsers.TENANT_ID, tenantId)
            .put(AJEntityUsers.PARTNER_ID, partnerId);
        this.redisClient.set(token, redisPacket.toString(), HelperConstants.RESET_PASS_TOKEN_EXPIRY);

        EmailNotificationBuilder emailNotificationBuilder = new EmailNotificationBuilder();
        emailNotificationBuilder.setTemplateName(EmailTemplateConstants.PASSWORD_CHANGE_REQUEST).addToAddress(email)
            .putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
            .putContext(ParameterConstants.PARAM_USER_ID, user.getString(AJEntityUsers.ID));

        return new ExecutionResult<>(MessageResponseFactory.createPostResponse(EventBuilderFactory
            .geTriggerResetPasswordEventBuilder(user.getString(AJEntityUsers.ID), emailNotificationBuilder)),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

}
