package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru on 28-Jun-2018
 */
public class InitLoginProcessor extends AbstractCommandProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitLoginProcessor.class);
	
	protected InitLoginProcessor(ProcessorContext context) {
		super(context);
	}

	@Override
	protected void setDeprecatedVersions() {
		// NOOP
	}

	@Override
	protected MessageResponse processCommand() {
		try {
			return RepoBuilder.buildAuthenticationRepo(context).initLogin();
		} catch (Throwable t) {
			LOGGER.error("exception while domain based redirection", t);
			return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
		}
	}

}
