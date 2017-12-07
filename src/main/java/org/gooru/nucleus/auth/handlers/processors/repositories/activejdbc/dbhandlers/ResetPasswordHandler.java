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
public class ResetPasswordHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private final RedisClient redisClient;
    private String token;
    private String password;
    private AJEntityUsers user;
    private String email;
    private String tenantId;
    private String partnerId;

    public ResetPasswordHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            AJEntityUsers.resetPasswordFieldSelector(), AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        token = context.requestBody().getString(ParameterConstants.PARAM_TOKEN);
        password = context.requestBody().getString(AJEntityUsers.PASSWORD);

        LOGGER.debug("checkSanity OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        JsonObject redisPacket = redisClient.getJsonObject(token);
        if (redisPacket == null) {
            LOGGER.warn("packet not found in redis for the token:{}", token);
            return new ExecutionResult<>(MessageResponseFactory.createGoneResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        email = redisPacket.getString(AJEntityUsers.EMAIL);
        tenantId = redisPacket.getString(AJEntityUsers.TENANT_ID);
        partnerId = redisPacket.getString(AJEntityUsers.PARTNER_ID);
        user = DBHelper.getUserByEmailAndTenantId(email, tenantId, partnerId);
        if (user == null) {
            LOGGER.warn("user not found in database for email: {} and Tenant: {}, Partner: {}", email, tenantId,
                partnerId);
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        String loginType = user.getString(AJEntityUsers.LOGIN_TYPE);
        if (!loginType.equalsIgnoreCase(HelperConstants.UserLoginType.credential.getType())) {
            LOGGER.warn("login type of the user is not credential");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
        LOGGER.debug("validateRequest OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        user.setString(AJEntityUsers.PASSWORD, InternalHelper.encryptPassword(password));
        user.saveIt();

        redisClient.del(token);

        final String newToken = InternalHelper.generatePasswordResetToken(user.getString(AJEntityUsers.ID));
        JsonObject redisPacket = new JsonObject().put(AJEntityUsers.EMAIL, email).put(AJEntityUsers.TENANT_ID, tenantId)
            .put(AJEntityUsers.PARTNER_ID, partnerId);
        this.redisClient.set(newToken, redisPacket.toString(), HelperConstants.RESET_PASS_TOKEN_EXPIRY);

        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(EventBuilderFactory
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
