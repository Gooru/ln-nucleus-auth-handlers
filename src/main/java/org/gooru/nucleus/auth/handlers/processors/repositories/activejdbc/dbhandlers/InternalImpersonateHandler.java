package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.Base64;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
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
 * @author szgooru
 *         Created On: 03-Jan-2017
 */
public class InternalImpersonateHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalImpersonateHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private static String basicCredentials;
    private static String clientId;
    private static String clientKey;
    private static AJEntityPartner partner;
    private static AJEntityTenant tenant;
    private static AJEntityUsers user;

    public InternalImpersonateHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator()
            .validatePayload(context.requestBody(), RequestValidator.authorizeFieldSelector(),
                RequestValidator.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        String grantType = context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE);
        if (!grantType.equalsIgnoreCase(HelperConstants.GrantTypes.credential.getType())) {
            LOGGER.warn("invalid grant type in request");
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
        LazyList<AJEntityPartner> partners = AJEntityPartner
            .findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, clientId, InternalHelper.encryptClientKey(clientKey));
        if (partners.isEmpty()) {
            tenants = AJEntityTenant
                .findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, clientId, InternalHelper.encryptClientKey(clientKey),
                    HelperConstants.GrantTypes.credential.getType());
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

        byte credentialsDecoded[] = Base64.getDecoder().decode(basicCredentials);
        final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
        final String[] credentials = credential.split(":");
        String userId = credentials[0];
        LOGGER.debug("userid: {}", userId);

        LazyList<AJEntityUsers> users =
            AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_TENANT_ID, userId, tenant.getString(AJEntityTenant.ID));
        if (users.isEmpty()) {
            LOGGER.warn("user not found in database for id: {}, tenant_id:{}", userId,
                tenant.getString(AJEntityTenant.ID));
            return new ExecutionResult<>(
                MessageResponseFactory.createUnauthorizedResponse((RESOURCE_BUNDLE.getString("user.not.found"))),
                ExecutionStatus.FAILED);
        }

        user = users.get(0);

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        final JsonObject result = new ResoponseBuilder(context, user, tenant, partner).build();

        LOGGER.debug("user token generated successfully");
        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(result),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

}
