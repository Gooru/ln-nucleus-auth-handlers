package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gooru
 * Created On: 03-Jan-2017
 *
 */
public class SinginUserProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinginUserProcessor.class);

    protected SinginUserProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
             LOGGER.info("signing in user");
             return RepoBuilder.buildAuthenticationRepo(context).signinUser();
        } catch (Throwable t) {
            LOGGER.error("exception while user signin", t.getMessage());
            return MessageResponseFactory.createInvalidRequestResponse(t.getMessage());
        }
    }

}
