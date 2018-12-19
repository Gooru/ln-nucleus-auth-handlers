package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalTenantRealmProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalTenantRealmProcessor.class);

  protected InternalTenantRealmProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("processing internal tenant realm");
      return RepoBuilder.buildInternalRepo(context).tenantRealm();
    } catch (Throwable t) {
      LOGGER.error("exception while internal tenant realm", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
