package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.constants.EmailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.emails.EmailNotificationBuilder;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.DBHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.TenantHelper;
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
    private boolean isPartner = false;

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
        ExecutionResult<MessageResponse> result = AuthorizerBuilder.buildAppAuthorizer(context).authorize(null);
        if (!result.continueProcessing()) {
            return result;
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
            isPartner = true;
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
        String tenantId = tenant.getString(AJEntityTenant.ID);
        String partnerId = isPartner ? partner.getString(AJEntityPartner.ID) : null;
        
        LazyList<AJEntityUsers> users;
        if (isPartner) {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_PARTNER_ID, identityId.toLowerCase(),
                partnerId);
        } else {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, identityId.toLowerCase(),
                tenantId);
        }
        
        EventBuilder eb;
        if (users.isEmpty()) {
            LOGGER.debug("user not found in database for email '{}', tenant '{}', partner '{}'", identityId,
                tenantId, partnerId);
            user = new AJEntityUsers();
            user.set(AJEntityUsers.TENANT_ID, getPGObject(tenantId));
            user.setTenantRoot(TenantHelper.getTenantRoot(tenantId));
            user.set(AJEntityUsers.PARTNER_ID, getPGObject(partnerId));
            user.setString(AJEntityUsers.FIRST_NAME, userJson.getString(AJEntityUsers.FIRST_NAME, null));
            user.setString(AJEntityUsers.LAST_NAME, userJson.getString(AJEntityUsers.LAST_NAME, null));
            user.setString(AJEntityUsers.EMAIL, userJson.getString(ParameterConstants.PARAM_IDENTITY_ID).toLowerCase());
            user.setString(AJEntityUsers.LOGIN_TYPE,
                context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE));

            // To make all SSO flows consistent, removed the auto population of the
            // username.
            String username = userJson.getString(AJEntityUsers.USERNAME);
            if (username != null) {
                AJEntityUsers existingUser = DBHelper.getUserByUsername(username, tenantId, partnerId, isPartner);
                if (existingUser != null) {
                    LOGGER.info("username '{}' already taken, setting it to null", username);
                    user.setString(AJEntityUsers.USERNAME, null);
                }
                user.setString(AJEntityUsers.DISPLAY_NAME, username);
            }

            if (!user.insert()) {
                LOGGER.debug("unable to create new user");
                return new ExecutionResult<>(
                    MessageResponseFactory.createInvalidRequestResponse("Unable to create user"),
                    ExecutionStatus.FAILED);
            }
            EmailNotificationBuilder emailNotificationBuilder = new EmailNotificationBuilder();
            emailNotificationBuilder.setTemplateName(EmailTemplateConstants.WELCOME_MAIL)
                .addToAddress(user.getString(AJEntityUsers.EMAIL));
            eb = EventBuilderFactory.getSignupUserEventBuilder(user.getString(AJEntityUsers.ID),
                emailNotificationBuilder);
        } else {
            user = users.get(0);
            eb = EventBuilderFactory.getSigninUserEventBuilder(user.getString(AJEntityUsers.ID));
        }

        final JsonObject result = new ResoponseBuilder(context, user, tenant, partner).build();

        return new ExecutionResult<>(MessageResponseFactory.createPostResponse(result, eb), ExecutionStatus.SUCCESSFUL);
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
