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
 * @author szgooru Created On: 25-Jan-2017
 */
public class ChangePassowrdHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePassowrdHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private final RedisClient redisClient;
    private String userId;
    private String oldPassword;
    private String newPassword;
    private AJEntityUsers user;

    public ChangePassowrdHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.requestBody(), AJEntityUsers.changePasswordFieldSelector(),
                AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        oldPassword = context.requestBody().getString(ParameterConstants.PARAM_OLD_PASSWORD);
        newPassword = context.requestBody().getString(ParameterConstants.PARAM_NEW_PASSWORD);
        userId = context.user().getString(ParameterConstants.PARAM_USER_ID);

        LOGGER.debug("checkSanity OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        user = DBHelper.getUserById(userId);
        if (user == null) {
            LOGGER.warn("user not found in database for id: {}", userId);
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        String loginType = user.getString(AJEntityUsers.LOGIN_TYPE);
        if (!loginType.equalsIgnoreCase(HelperConstants.UserLoginType.credential.getType())) {
            LOGGER.warn("login type of the user is not credential");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        final String encryptedPassword = InternalHelper.encryptPassword(oldPassword);
        if (!encryptedPassword.equals(user.getString(AJEntityUsers.PASSWORD))) {
            LOGGER.warn("Invalid old password provided");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse((RESOURCE_BUNDLE.getString("invalid.password"))),
                ExecutionStatus.FAILED);
        }
        LOGGER.debug("vaidateRequest OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        user.setString(AJEntityUsers.PASSWORD, InternalHelper.encryptPassword(newPassword));
        user.saveIt();

        final String newToken = InternalHelper.generatePasswordResetToken(user.getString(AJEntityUsers.ID));
        JsonObject redisPacket = new JsonObject().put(AJEntityUsers.EMAIL, user.getString(AJEntityUsers.EMAIL))
            .put(AJEntityUsers.TENANT_ID, user.getString(AJEntityUsers.TENANT_ID));
        this.redisClient.set(newToken, redisPacket.toString(), HelperConstants.RESET_PASS_TOKEN_EXPIRY);

        return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(EventBuilderFactory
            .getResetPasswordEventBuilder(user.getString(AJEntityUsers.ID))),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

}
