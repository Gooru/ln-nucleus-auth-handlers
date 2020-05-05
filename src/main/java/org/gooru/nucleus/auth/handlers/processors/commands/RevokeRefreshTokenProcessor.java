package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RevokeRefreshTokenProcessor extends AbstractCommandProcessor {

  private final Logger LOGGER = LoggerFactory.getLogger(RevokeRefreshTokenProcessor.class);

  RevokeRefreshTokenProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("processing revoke refresh token.");
      return RepoBuilder.buildAuthenticationRepo(context).revokeRefreshToken();
    } catch (Throwable t) {
      LOGGER.error("exception while revoking refresh token", t.getMessage());
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
