package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserAccounts;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserAccountsHandler implements DBHandler, PayloadValidator {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountsHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

  private String email;
  private static final String USER_ACCOUNTS = "user_accounts";

  public UserAccountsHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    JsonObject errors = validatePayload(context.requestBody(),
        AJEntityUserAccounts.mandatoryFields(), AJEntityUserAccounts.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    email = context.requestBody().getString(AJEntityUsers.EMAIL);
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LOGGER.debug("Email: {}", email);
    LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL, email);
    if (users.isEmpty()) {
      LOGGER.warn("user not found in database for email: {}", email);
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
          (RESOURCE_BUNDLE.getString("user.not.found"))), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    List<Map> accounts = Base.findAll(AJEntityUserAccounts.SELECT_ACCOUNTS_BY_EMAIL, email);
    JsonArray accountsAsJsonArray = new JsonArray(accounts);
    final JsonObject response = new JsonObject();
    response.put(USER_ACCOUNTS, accountsAsJsonArray);
    return new ExecutionResult<>(MessageResponseFactory.createPostResponse(response),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
