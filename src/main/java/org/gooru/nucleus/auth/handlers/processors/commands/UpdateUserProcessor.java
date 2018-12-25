package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 03-Jan-2017
 */
class UpdateUserProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserProcessor.class);

  UpdateUserProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("updating user");
      return RepoBuilder.buildUserRepo(context).updateUser();
    } catch (Throwable t) {
      LOGGER.error("exception while updating user");
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
