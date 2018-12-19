package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public class GetAccessTokenDetailsProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GetAccessTokenDetailsProcessor.class);

  GetAccessTokenDetailsProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      return RepoBuilder.buildAuthenticationRepo(context).getAccessTokenDetails();
    } catch (Throwable t) {
      LOGGER.error("exception while getting access token details", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }
}
