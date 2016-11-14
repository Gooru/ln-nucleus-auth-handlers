package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.constants.CommandConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalMessageProcessor implements MessageProcessorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalMessageProcessor.class);
    
    @Override
    public MessageResponse process(MessageContext messageContext) {
        MessageResponse result = null;
        switch (messageContext.command()) {
        case CommandConstants.INTERNAL_AUTHENTICATE:
            result = RepoFactory.getInternalRepo(messageContext).authenticate();
            break;
            
        case CommandConstants.INTERNAL_IMPERSONATE:
            result = RepoFactory.getInternalRepo(messageContext).impersonate();
            break;
        
        case CommandConstants.INTERNAL_LTI_SSO:
            result = RepoFactory.getInternalRepo(messageContext).sso();
            break;
            
        default:
            LOGGER.error("Invalid command type passed in, not able to handle");
            throw new InvalidRequestException();
        }
        return result;
    }

}
