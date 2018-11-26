package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalTenantRelamProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalTenantRelamProcessor.class);

    protected InternalTenantRelamProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("processing internal tenant relam");
            return RepoBuilder.buildInternalRepo(context).tenantRelam();
        } catch (Throwable t) {
            LOGGER.error("exception while internal tenant relam", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

}
