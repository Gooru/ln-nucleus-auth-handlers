package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 05-Jan-2017
 */
public class GetUserTokenDetailsHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetUserTokenDetailsHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private AJEntityUsers user;
    private UUID userId;
    private UUID tenantId;

    public GetUserTokenDetailsHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        try {
            userId = UUID.fromString(context.user().getString(ParameterConstants.PARAM_USER_ID));
            tenantId = UUID.fromString(context.user().getJsonObject(ParameterConstants.PARAM_TENANT)
                .getString(ParameterConstants.PARAM_TENANT_ID));
        } catch (IllegalArgumentException iae) {
            LOGGER.warn("invalid user id format");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_TENANT_ID, userId, tenantId);
        if (users.isEmpty()) {
            LOGGER.warn("user not found in database for id: {}, tenant_id:{}", userId.toString(), tenantId.toString());
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        user = users.get(0);

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject result = context.user().copy();
        result.put(AJEntityUsers.FIRST_NAME, user.getString(AJEntityUsers.FIRST_NAME));
        result.put(AJEntityUsers.LAST_NAME, user.getString(AJEntityUsers.LAST_NAME));
        result.put(AJEntityUsers.USER_CATEGORY, user.getString(AJEntityUsers.USER_CATEGORY));
        result.put(AJEntityUsers.THUMBNAIL, user.getString(AJEntityUsers.THUMBNAIL));
        
        LOGGER.debug("user token details fetched successfully");
        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(result),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
