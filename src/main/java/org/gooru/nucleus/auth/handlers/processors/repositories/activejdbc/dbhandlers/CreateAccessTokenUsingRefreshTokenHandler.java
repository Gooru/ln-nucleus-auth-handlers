package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
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

public class CreateAccessTokenUsingRefreshTokenHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CreateAccessTokenUsingRefreshTokenHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

  private String refreshToken;
  private AJEntityPartner partner;
  private AJEntityTenant tenant;
  private AJEntityUsers user;
  private final RedisClient redisClient;


  public CreateAccessTokenUsingRefreshTokenHandler(ProcessorContext context) {
    this.context = context;
    this.redisClient = RedisClient.instance();
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    refreshToken = context.headers().get(MessageConstants.MSG_HEADER_REFRESH_TOKEN);
    if (refreshToken == null || refreshToken.isEmpty()) {
      LOGGER.warn("invalid refresh token in request");
      return new ExecutionResult<>(MessageResponseFactory.createUnauthorizedResponse(
          RESOURCE_BUNDLE.getString("invalid.refresh.token")), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    return validateRefreshTokenDetails();
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    final JsonObject result = new ResoponseBuilder(context, user, tenant, partner).build();

    LOGGER.debug("user token generated successfully using refresh");
    return new ExecutionResult<>(
        MessageResponseFactory.createGetResponse(result, EventBuilderFactory
            .getCreateAccessTokenUsingRefreshTokenEventBuilder(user.getString(AJEntityUsers.ID))),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private ExecutionResult<MessageResponse> validateRefreshTokenDetails() {
    String extractedUserIdFromRefreshToken =
        InternalHelper.extractUserIdFromRefreshToken(refreshToken);
    JsonObject refreshTokenDetails = this.redisClient
        .getJsonObject(InternalHelper.generateRefreshTokenKey(extractedUserIdFromRefreshToken));
    if (refreshTokenDetails == null) {
      LOGGER.debug("Invalid refresh token, does exists in redis : {}", refreshToken);
      return new ExecutionResult<>(MessageResponseFactory.createUnauthorizedResponse(
          (RESOURCE_BUNDLE.getString("invalid.refresh.token"))), ExecutionStatus.FAILED);
    }

    final String userRefreshToken =
        refreshTokenDetails.getString(ParameterConstants.PARAM_REFRESH_TOKEN);
    if (userRefreshToken == null || !userRefreshToken.equals(refreshToken)) {
      LOGGER.debug("Invalid refresh token: {}", this.refreshToken);
      return new ExecutionResult<>(MessageResponseFactory.createUnauthorizedResponse(
          (RESOURCE_BUNDLE.getString("invalid.refresh.token"))), ExecutionStatus.FAILED);
    }

    String userId = refreshTokenDetails.getString(ParameterConstants.PARAM_USER_ID);
    if (userId == null) {
      LOGGER.debug("Invalid user id");
      return new ExecutionResult<>(MessageResponseFactory.createUnauthorizedResponse(
          (RESOURCE_BUNDLE.getString("user.not.found"))), ExecutionStatus.FAILED);
    }
    LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID, userId);
    if (users == null || users.isEmpty()) {
      LOGGER.warn("user not found in database for  this id: {}", userId);
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
          (RESOURCE_BUNDLE.getString("user.not.found"))), ExecutionStatus.FAILED);
    }

    user = users.get(0);

    String tenantId = user.getString(AJEntityUsers.TENANT_ID);

    LazyList<AJEntityTenant> tenants =
        AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID, tenantId);
    if (tenants == null || tenants.isEmpty()) {
      LOGGER.warn("Tenant Not found  '{}'", tenantId);
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(
          RESOURCE_BUNDLE.getString("tenant.not.found")), ExecutionStatus.FAILED);
    }

    tenant = tenants.get(0);

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }


  @Override
  public boolean handlerReadOnly() {
    return true;
  }


}
