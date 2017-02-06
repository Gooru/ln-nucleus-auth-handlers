package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru
 *         Created On: 25-Jan-2017
 */
public class ChangePasswordProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePasswordProcessor.class);

    ChangePasswordProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("change password for user");
            return RepoBuilder.buildUserRepo(context).changePassword();
        } catch (Throwable t) {
            LOGGER.error("exception while changing password");
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

}
