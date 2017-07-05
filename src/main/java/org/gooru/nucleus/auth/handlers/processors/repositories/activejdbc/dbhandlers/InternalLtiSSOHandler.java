package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.RequestValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.responses.ResoponseBuilder;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 03-Jan-2017
 */
public class InternalLtiSSOHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalLtiSSOHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private static String clientId;
    private static String clientKey;
    private static AJEntityPartner partner;
    private static AJEntityTenant tenant;
    private static AJEntityUsers user;

    public InternalLtiSSOHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {

        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.requestBody(), RequestValidator.ltissoFieldSelector(),
                RequestValidator.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        String grantType = context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE);
        if (!grantType.equalsIgnoreCase(HelperConstants.GrantTypes.ltisso.getType())) {
            LOGGER.warn("invalid grant type in request");
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse(RESOURCE_BUNDLE.getString("invalid.granttype")),
                ExecutionStatus.FAILED);
        }
        // TODO:
        // Validate user payload from request

        final String basicCredentials = context.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
        if (basicCredentials == null || basicCredentials.isEmpty()) {
            LOGGER.warn("invalid credentials in request");
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse(RESOURCE_BUNDLE.getString("invalid.credential")),
                ExecutionStatus.FAILED);
        }
        
        final String credentials[] = InternalHelper.getClientIdAndSecret(basicCredentials);
        clientId = credentials[0];
        clientKey = credentials[1];

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        // validate app id if required
        ExecutionResult<MessageResponse> result = AuthorizerBuilder.buildAppAuthorizer(context).authorize(null);
        if (!result.continueProcessing()) {
            return result;
        }
        
        LazyList<AJEntityTenant> tenants;

        // First lookup in partner if not found, fall back on tenant
        LazyList<AJEntityPartner> partners = AJEntityPartner
            .findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, clientId, InternalHelper.encryptClientKey(clientKey));
        if (partners.isEmpty()) {
            tenants = AJEntityTenant
                .findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, clientId, InternalHelper.encryptClientKey(clientKey),
                    HelperConstants.GrantTypes.ltisso.getType());
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

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject userObject = context.requestBody().getJsonObject(ParameterConstants.PARAM_USER);
        String referenceId = userObject.getString(AJEntityUsers.REFERENCE_ID);
        LazyList<AJEntityUsers> users =
            AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_REFERENCE_ID_TENANT_ID, referenceId, clientId);
        if (users.isEmpty()) {
            LOGGER.debug("user not found in database for reference_id: {}, client_id: {}", referenceId, clientId);
            user = new AJEntityUsers();
            user.setString(AJEntityUsers.LOGIN_TYPE, HelperConstants.UserLoginType.ltisso.getType());
            user.setTenantId(clientId);
            autoPopulate();

            if (user.hasErrors()) {
                LOGGER.warn("Validation errors while populating entity");
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
            
            if (!user.insert()) {
                LOGGER.debug("unable to create new user");
                return new ExecutionResult<>(
                    MessageResponseFactory.createInvalidRequestResponse("Unable to create user"),
                    ExecutionStatus.FAILED);
            }
        } else {
            user = users.get(0);
        }

        final JsonObject result = new ResoponseBuilder(context, user, tenant, partner).build();

        return new ExecutionResult<>(MessageResponseFactory
            .createPostResponse(result, EventBuilderFactory.getLTISSOEventBuilder(user.getString(AJEntityUsers.ID))),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private void autoPopulate() {
        new DefaultAJEntityUsersBuilder()
            .build(user, context.requestBody().getJsonObject(ParameterConstants.PARAM_USER),
                AJEntityUsers.getConverterRegistry());
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityUsersBuilder implements EntityBuilder<AJEntityUsers> {
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        user.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }
}
