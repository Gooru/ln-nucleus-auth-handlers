
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.javalite.activejdbc.LazyList;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On 14-Feb-2019
 */
public class SignoutHandler implements DBHandler {

  private final static Logger LOGGER = LoggerFactory.getLogger(SignoutHandler.class);

  private final ProcessorContext context;
  private final RedisClient redisClient = RedisClient.instance();

  private JsonObject sessionPacket = null;
  private String loginUrl = null;

  public SignoutHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    this.sessionPacket = redisClient.getJsonObject(context.accessToken());

    // We do not want to error out if the session is not found in redis. Hence returning success
    // response.
    if (this.sessionPacket == null || this.sessionPacket.isEmpty()) {
      LOGGER.debug("no session found in redis, returning success");
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    JsonObject tenantJson = this.sessionPacket.getJsonObject(ParameterConstants.PARAM_TENANT);
    // If no tenant details found in redis packet, return default login settings
    if (tenantJson == null || tenantJson.isEmpty()) {
      LOGGER.debug("No tenant details found in redis packet, returning success");
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(getResponseJson(null)),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    String tenantId = tenantJson.getString(AJEntityUsers.TENANT_ID);
    LazyList<AJEntityTenant> tenants =
        AJEntityTenant.findBySQL(AJEntityTenant.SELECT_LOGIN_URL, tenantId);
    // If tenant not found in the database, return success
    if (tenants.isEmpty()) {
      LOGGER.debug("tenant not found in database, returning success");
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(getResponseJson(null)),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    AJEntityTenant tenant = tenants.get(0);
    this.loginUrl = tenant.getString(AJEntityTenant.LOGIN_URL);
    LOGGER.debug("logging out for tenant '{}'", tenantId);

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String userId = sessionPacket.getString(ParameterConstants.PARAM_USER_ID);
    RedisClient.instance().del(context.accessToken());
    // refresh token will get deleted, if the token associated with this user.
    RedisClient.instance().del(InternalHelper.generateRefreshTokenKey(userId));

    // Do not send event for anonymous user signout
    if (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      return new ExecutionResult<>(
          MessageResponseFactory.createGetResponse(getResponseJson(this.loginUrl)),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    return new ExecutionResult<>(
        MessageResponseFactory.createGetResponse(getResponseJson(this.loginUrl),
            EventBuilderFactory.getSignoutUserEventBuilder(userId)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

  private JsonObject getResponseJson(String loginUrl) {
    return new JsonObject()
        .put(ParameterConstants.RESP_KEY_DEFAULT,
            (loginUrl == null || loginUrl.isEmpty() ? true : false))
        .put(ParameterConstants.RESP_KEY_LOGINURL, loginUrl);
  }

}
