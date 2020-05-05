
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class RevokeRefreshTokenHandler implements DBHandler {

  private final static Logger LOGGER = LoggerFactory.getLogger(RevokeRefreshTokenHandler.class);

  private final ProcessorContext context;
  private final RedisClient redisClient = RedisClient.instance();

  private JsonObject sessionPacket = null;

  public RevokeRefreshTokenHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    this.sessionPacket = redisClient.getJsonObject(context.accessToken());

    if (this.sessionPacket == null || this.sessionPacket.isEmpty()) {
      LOGGER.debug("no session found in redis, returning failure");
      return new ExecutionResult<>(MessageResponseFactory.createUnauthorizedResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String userId = sessionPacket.getString(ParameterConstants.PARAM_USER_ID);
    // refresh token will get deleted, if the token associated with this user.
    RedisClient.instance().del(InternalHelper.generateRefreshTokenKey(userId));

    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }


}
