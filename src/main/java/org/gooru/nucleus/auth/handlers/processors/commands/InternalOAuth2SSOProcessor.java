package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InternalOAuth2SSOProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalOAuth2SSOProcessor.class);

  protected InternalOAuth2SSOProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("processing internal oauth2 sso");
      return RepoBuilder.buildInternalRepo(context).oauth2sso();
    } catch (Throwable t) {
      LOGGER.error("exception while internal oauth2 sso", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
