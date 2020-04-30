package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CreateAccessTokenUsingRefreshTokenProcessor extends AbstractCommandProcessor {

  private final Logger LOGGER =
      LoggerFactory.getLogger(CreateAccessTokenUsingRefreshTokenProcessor.class);

  CreateAccessTokenUsingRefreshTokenProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("processing create access token using refresh token");
      return RepoBuilder.buildAuthenticationRepo(context).createAccessTokenUsingRefreshToken();
    } catch (Throwable t) {
      LOGGER.error("exception while create access token using refresh token", t.getMessage());
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
