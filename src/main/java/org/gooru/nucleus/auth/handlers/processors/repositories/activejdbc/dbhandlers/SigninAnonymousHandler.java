package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.DBHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.TenantHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.RequestValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public class SigninAnonymousHandler implements DBHandler {

  private final RedisClient redisClient;
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(SigninAnonymousHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

  private String clientId;
  private String clientKey;
  private boolean isNonceExist;
  private AJEntityPartner partner;
  private AJEntityTenant tenant;

  public SigninAnonymousHandler(ProcessorContext context) {
    this.context = context;
    this.redisClient = RedisClient.instance();
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

    String grantType = context.requestBody().getString(ParameterConstants.PARAM_GRANT_TYPE);
    if (!grantType.equalsIgnoreCase(HelperConstants.GrantTypes.anonymous.getType())) {
      LOGGER.warn("missing or invalid grant type in request");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(
          RESOURCE_BUNDLE.getString("invalid.granttype")), ExecutionStatus.FAILED);
    }
    return checkClientDetailsOrNonce();

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // validate app id if required
    ExecutionResult<MessageResponse> result =
        AuthorizerBuilder.buildAppAuthorizer(context).authorize(null);
    if (!result.continueProcessing()) {
      return result;
    }

    LazyList<AJEntityTenant> tenants;

    // First lookup in partner if not found, fall back on tenant
    LazyList<AJEntityPartner> partners = null;
    if (isNonceExist) {
      partners = AJEntityPartner.findBySQL(AJEntityPartner.SELECT_BY_ID, clientId);
    } else {
      partners = AJEntityPartner.findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, clientId,
          InternalHelper.encryptClientKey(clientKey));
    }

    if (partners.isEmpty()) {
      if (isNonceExist) {
        tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID_GRANT_TYPE, clientId,
            HelperConstants.GrantTypes.anonymous.getType());
      } else {
        tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, clientId,
            InternalHelper.encryptClientKey(clientKey),
            HelperConstants.GrantTypes.anonymous.getType());
      }
    } else {
      partner = partners.get(0);
      tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID,
          partner.getString(AJEntityPartner.TENANT_ID));
    }

    if (tenants.isEmpty()) {
      LOGGER.warn("No matching partner or tenant found for client_id '{}' and client_key '{}'",
          clientId, clientKey);
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(
          RESOURCE_BUNDLE.getString("tenant.not.found")), ExecutionStatus.FAILED);
    }

    tenant = tenants.get(0);
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject result = new JsonObject();
    String tenantId = tenant.getString(AJEntityTenant.ID);
    String partnerId = (partner != null) ? partner.getString(AJEntityPartner.ID) : null;
    final String accessToken =
        InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS, partnerId, tenantId);
    result.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    result.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    result.put(ParameterConstants.PARAM_CDN_URLS,
        new JsonObject(tenant.getString(AJEntityTenant.CDN_URLS)));

    JsonObject tenantJson = new JsonObject();
    tenantJson.put(AJEntityUsers.TENANT_ID, tenantId);
    tenantJson.put(AJEntityUsers.TENANT_ROOT, TenantHelper.getTenantRoot(tenantId));
    tenantJson.put(AJEntityTenant.TENANT_NAME, tenant.getString(AJEntityTenant.NAME));
    tenantJson.put(AJEntityTenant.TENANT_SHORT_NAME, tenant.getString(AJEntityTenant.SHORT_NAME));
    tenantJson.put(AJEntityTenant.TENANT_IMAGE_URL, tenant.getString(AJEntityTenant.IMAGE_URL));
    tenantJson.put(ParameterConstants.PARAM_SETTINGS,
        DBHelper.getTenantSettings(tenant.getString(AJEntityTenant.ID)));
    result.put(ParameterConstants.PARAM_TENANT, tenantJson);

    JsonObject userPreference = TenantHelper.mergeDefaultPrefsWithTenantFrameworkPrefs(tenantId);
    result.put(AJEntityUserPreference.PREFERENCE_SETTINGS, userPreference);

    int accessTokenValidity =
        (partner != null) ? partner.getInteger(AJEntityPartner.ACCESS_TOKEN_VALIDITY)
            : tenant.getInteger(AJEntityTenant.ACCESS_TOKEN_VALIDITY);

    // Save access token with details in redis
    saveAccessToken(accessToken, result, accessTokenValidity);

    result.put(ParameterConstants.PARAM_ACCESS_TOKEN, accessToken);

    LOGGER.debug("anonymous token generated successfully");
    return new ExecutionResult<>(
        MessageResponseFactory.createGetResponse(result,
            EventBuilderFactory.getAnonymousSigninEventBuilder(clientId)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

  private ExecutionResult<MessageResponse> checkClientDetailsOrNonce() {
    clientId = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_ID);
    clientKey = context.requestBody().getString(ParameterConstants.PARAM_CLIENT_KEY);
    if (clientId != null && clientKey != null) {
      return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    } else {
      String nonce = context.headers().get(MessageConstants.MSG_HEADER_NONCE_TOKEN);
      if (nonce == null) {
        LOGGER.warn(RESOURCE_BUNDLE.getString("missing.client.details.or.nonce"));
        return new ExecutionResult<>(
            MessageResponseFactory.createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("missing.client.details.or.nonce")),
            ExecutionResult.ExecutionStatus.FAILED);
      }
      isNonceExist = true;
      clientId = this.getClientIdUsingNonceToken(nonce);
      if (clientId == null) {
        LOGGER.warn(RESOURCE_BUNDLE.getString("invalid.nonce.token"));
        return new ExecutionResult<>(
            MessageResponseFactory
                .createUnauthorizedResponse(RESOURCE_BUNDLE.getString("invalid.nonce.token")),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  private void saveAccessToken(String token, JsonObject session, Integer expireAtInSeconds) {
    session.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
    this.redisClient.set(token, session.toString(), expireAtInSeconds);
  }

  private String getClientIdUsingNonceToken(String nonce) {
    return this.redisClient.get(nonce);
  }

  private static class DefaultPayloadValidator implements PayloadValidator {
  }
}
