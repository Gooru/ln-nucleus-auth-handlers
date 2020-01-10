package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAccountsProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountsProcessor.class);

    UserAccountsProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("Fetch User accounts");
            return RepoBuilder.buildUserRepo(context).getUserAccounts();
        } catch (Throwable t) {
            LOGGER.error("exception while fetching the accounts details", t);
            return MessageResponseFactory.createInvalidRequestResponse(t.getMessage());
        }
    }
}
