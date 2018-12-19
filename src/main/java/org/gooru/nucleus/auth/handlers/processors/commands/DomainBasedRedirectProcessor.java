package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 23-Oct-2017
 */
public class DomainBasedRedirectProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainBasedRedirectProcessor.class);

  protected DomainBasedRedirectProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      return RepoBuilder.buildAuthenticationRepo(context).domainBasedRedirect();
    } catch (Throwable t) {
      LOGGER.error("exception while domain based redirection", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
