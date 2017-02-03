package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.DBHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author gooru Created On: 03-Jan-2017
 *
 */
public class UpdateUserHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private AJEntityUsers user;

    public UpdateUserHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // TODO: revisit the field validation for null/nullable fields
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            AJEntityUsers.updateFieldSelector(), AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        String userId = context.user().getString(ParameterConstants.PARAM_USER_ID);
        String tenantId =
            context.user().getJsonObject(ParameterConstants.PARAM_TENANT).getString(ParameterConstants.PARAM_TENANT_ID);

        user = DBHelper.getUserByIdAndTenantId(userId, tenantId);
        if (user == null) {
            LOGGER.warn("user not found for id:{}, tenant_id:{}", userId, tenantId);
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        // TODO: Do we need to update the Redis session if the username is
        // udpated?
        autoPopulate();

        if (!user.save()) {
            LOGGER.warn("unable to update user for id:{}", user.get(AJEntityUsers.ID));
            return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(
                    new JsonObject().put(MessageConstants.MSG_MESSAGE, RESOURCE_BUNDLE.getString("user.save.error"))),
                ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(
                EventBuilderFactory.getUpdateUserEventBuilder(user.getString(AJEntityUsers.ID))),
            ExecutionStatus.SUCCESSFUL);
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
