package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.auth.handlers.app.components.AppConfiguration;
import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalTenantRealmHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalTenantRealmHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("message");

  private final ProcessorContext context;
  private String shortName;
  private AJEntityTenant tenant;
  private final RedisClient redisClient;

  public InternalTenantRealmHandler(ProcessorContext context) {
    this.context = context;
    this.redisClient = RedisClient.instance();
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    shortName = context.headers().get(ParameterConstants.PARAM_SHORT_NAME);
    if (shortName == null || shortName.isEmpty()) {
      LOGGER.warn("shortname is null or empty.");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityTenant> tenants =
        AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_SHORT_NAME_GRANT_TYPE, shortName.toLowerCase());
    if (tenants.isEmpty()) {
      LOGGER.warn("No tenant match with this shortname {}.", shortName);
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    this.tenant = tenants.get(0);
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    return redirectBasedonGrantType();
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private ExecutionResult<MessageResponse> redirectBasedonGrantType() {
    List<String> grantTypes = this.tenant.getGrantTypes();

    String tenantId = this.tenant.getString(AJEntityTenant.ID);
    if (grantTypes == null || grantTypes.isEmpty()) {
      LOGGER.warn("No grant type setup for the tenant '{}'", tenantId);
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("grant.type.not.defined")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    // Here we are trying to redirect the request to appropriate login page based on the grant type
    // available for tenant. Currently we are supporting credential and google grant types for
    // redirection. If we find any of these grant type at sequence one in grant types array we will
    // redirect based on that. Otherwise look for the second grant type in sequence and apply
    // redirect logic. We will only look for first two grant types in sequence, if not find any
    // supported match redirect the request to default login page.
    int count = 0;
    for (String grantType : grantTypes) {
      count++;
      if (grantType.equalsIgnoreCase(AJEntityTenant.GRANT_TYPE_CREDENTIAL)) {
        return credentialTypeRedirect(this.tenant.getString(AJEntityTenant.ID));
      } else if (grantType.equalsIgnoreCase(AJEntityTenant.GRANT_TYPE_GOOGLE)) {
        return googleTypeRedirect(this.tenant.getString(AJEntityTenant.ID));
      }

      if (count == 2) {
        break;
      }
    }

    return defaultRedirect();
  }

  private ExecutionResult<MessageResponse> credentialTypeRedirect(String tenantId) {
    String nonce = generateNonceAndsaveInRedis(tenantId);
    String redirectUrl = AppConfiguration.getInstance().credentialAppLoginUrl() + "?nonce=" + nonce;
    return new ExecutionResult<>(MessageResponseFactory.createMovePermanentlyResponse(redirectUrl),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private ExecutionResult<MessageResponse> googleTypeRedirect(String tenantId) {
    String redirectUrl =
        AppConfiguration.getInstance().googleAppLoginUrl() + tenantId;
    return new ExecutionResult<>(MessageResponseFactory.createMovePermanentlyResponse(redirectUrl),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private ExecutionResult<MessageResponse> defaultRedirect() {
    return new ExecutionResult<>(
        MessageResponseFactory
            .createMovePermanentlyResponse(AppConfiguration.getInstance().defaultLoginUrl()),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private String generateNonceAndsaveInRedis(String tenantId) {
    String nonce = UUID.randomUUID().toString();
    int nonceExpireInSecs = this.redisClient.getNonceExpireInSecs();
    this.redisClient.set(nonce, tenantId, nonceExpireInSecs);
    return nonce;
  }
}
