package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 12-Jul-2017
 */
public class InternalWSFedSSOProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalWSFedSSOProcessor.class);

  protected InternalWSFedSSOProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("processing internal wsfed sso");
      return RepoBuilder.buildInternalRepo(context).wsfedsso();
    } catch (Throwable t) {
      LOGGER.error("exception while internal wsfed sso", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
