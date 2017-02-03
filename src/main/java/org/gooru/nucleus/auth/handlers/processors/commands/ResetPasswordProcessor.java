package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru
 * Created On: 03-Jan-2017
 *
 */
public class ResetPasswordProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordProcessor.class);

    protected ResetPasswordProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("resetting password for user");
            return RepoBuilder.buildUserRepo(context).resetPassword();
        } catch (Throwable t) {
            LOGGER.error("exception while reset password");
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

}
