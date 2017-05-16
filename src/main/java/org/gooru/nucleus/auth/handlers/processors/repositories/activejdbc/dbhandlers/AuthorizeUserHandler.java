package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.Random;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.app.components.AppConfiguration;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityApp;
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
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 03-Jan-2017
 */
public class AuthorizeUserHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizeUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private static String clientId;
    private static String clientKey;
    private static AJEntityPartner partner;
    private static AJEntityTenant tenant;
    private static AJEntityUsers user;

    public AuthorizeUserHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            RequestValidator.authorizeFieldSelector(), RequestValidator.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        clientId = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_ID);
        clientKey = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_KEY);
        // TODO:
        // Validate user payload from request

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        // validate app id if required
        if (AppConfiguration.getInstance().isAppIdRequired()) {
            String appId = context.requestBody().getString(ParameterConstants.PARAM_APP_ID);
            LazyList<AJEntityApp> apps = AJEntityApp.findBySQL(AJEntityApp.VALIDATE_EXISTANCE, appId);
            if (apps.isEmpty()) {
                LOGGER.warn("app id '{}' not found", appId);
                return new ExecutionResult<>(
                    MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("appid.not.found")),
                    ExecutionStatus.FAILED);
            }
        }

        LazyList<AJEntityTenant> tenants;

        // First lookup in partner if not found, fall back on tenant
        LazyList<AJEntityPartner> partners = AJEntityPartner.findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, clientId,
            InternalHelper.encryptClientKey(clientKey));
        if (partners.isEmpty()) {
            tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, clientId,
                InternalHelper.encryptClientKey(clientKey), HelperConstants.GrantTypes.google.getType());
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
        JsonObject userJson = context.requestBody().getJsonObject(ParameterConstants.PARAM_USER);
        String identityId = userJson.getString(ParameterConstants.PARAM_IDENTITY_ID);
        LazyList<AJEntityUsers> users =
            AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, identityId.toLowerCase(), clientId);
        if (users.isEmpty()) {
            LOGGER.debug("user not found in database for email or reference_id: {}, client_id: {}", identityId,
                clientId);
            user = new AJEntityUsers();
            user.set(AJEntityUsers.TENANT_ID, getPGObject(clientId));
            user.setString(AJEntityUsers.FIRST_NAME, userJson.getString(AJEntityUsers.FIRST_NAME, null));
            user.setString(AJEntityUsers.LAST_NAME, userJson.getString(AJEntityUsers.LAST_NAME, null));
            user.setString(AJEntityUsers.EMAIL, userJson.getString(ParameterConstants.PARAM_IDENTITY_ID).toLowerCase());
            user.setString(AJEntityUsers.LOGIN_TYPE,
                context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE));
            populateUsername(user);

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

        return new ExecutionResult<>(
            MessageResponseFactory.createPostResponse(result,
                EventBuilderFactory.getAuthorizeUserEventBuilder(user.getString(AJEntityUsers.ID))),
            ExecutionStatus.SUCCESSFUL);
    }

    private void populateUsername(AJEntityUsers user) {
        String firstName = user.getString(AJEntityUsers.FIRST_NAME);
        if (firstName != null && !firstName.isEmpty()) {
            StringBuilder username = new StringBuilder(firstName.replaceAll("\\s+", ""));
            String lastName = user.getString(AJEntityUsers.LAST_NAME);
            if (lastName != null && !lastName.isEmpty()) {
                username.append(lastName.substring(0, lastName.length() > 5 ? 5 : lastName.length()));
            }

            if (username.toString().length() > 29) {
                username = new StringBuilder(username.substring(0, 28));
            }

            AJEntityUsers existingUser = AJEntityUsers.findFirst(AJEntityUsers.SELECT_BY_USERNAME_TENANT_ID,
                username.toString().toLowerCase(), clientId);
            if (existingUser != null) {
                final Random randomNumber = new Random();
                username.append(randomNumber.nextInt(999));
            }

            user.setString(AJEntityUsers.USERNAME, username.toString().toLowerCase());
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private PGobject getPGObject(String value) {
        PGobject pgObject = new PGobject();
        pgObject.setType("uuid");
        try {
            pgObject.setValue(value);
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }
}
