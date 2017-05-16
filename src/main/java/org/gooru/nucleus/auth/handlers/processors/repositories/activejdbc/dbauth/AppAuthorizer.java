package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.app.components.AppConfiguration;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityApp;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 17-May-2017
 */
public class AppAuthorizer implements Authorizer<AJEntityApp> {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppAuthorizer.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    public AppAuthorizer(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(AJEntityApp model) {
        if (AppConfiguration.getInstance().isAppIdRequired()) {
            String appId = context.requestBody().getString(ParameterConstants.PARAM_APP_ID);
            if (appId == null || appId.isEmpty()) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createValidationErrorResponse(new JsonObject()
                        .put(ParameterConstants.PARAM_APP_ID, RESOURCE_BUNDLE.getString("missing.mandatory.field"))),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

            LazyList<AJEntityApp> apps = AJEntityApp.findBySQL(AJEntityApp.VALIDATE_EXISTANCE, appId);
            if (apps.isEmpty()) {
                LOGGER.warn("app id '{}' not found", appId);
                return new ExecutionResult<>(
                    MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("appid.not.found")),
                    ExecutionStatus.FAILED);
            }
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

}
