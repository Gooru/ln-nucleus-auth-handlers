package org.gooru.nucleus.auth.handlers.processors;

import org.gooru.nucleus.auth.handlers.processors.commands.CommandProcessorBuilder;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.VersionDeprecatedException;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.Message;

/**
 * @author szgooru
 * Created On: 30-Dec-2016
 *
 */
public class MessageProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
    private final Message<Object> message;

    public MessageProcessor(Message<Object> message) {
        this.message = message;
    }

    @Override
    public MessageResponse process() {
        try {
            ProcessorContext context = createContext();
            String operation = context.operation();
            LOGGER.info("## Processing Request - {} ##", operation);
            return CommandProcessorBuilder.lookupBuilder(operation).build(context).process();
        } catch (InvalidRequestException e) {
            LOGGER.error("Invalid request");
            return MessageResponseFactory.createInternalErrorResponse("Invalid request");
        } catch (InvalidUserException e) {
            LOGGER.error("User is not valid");
            return MessageResponseFactory.createForbiddenResponse();
        } catch (VersionDeprecatedException e) {
            LOGGER.error("Version is deprecated");
            return MessageResponseFactory.createVersionDeprecatedResponse();
        } catch (Throwable t) {
            LOGGER.error("Exception while processing request");
            MessageResponseFactory.createInternalErrorResponse("something went wrong in database transaction");
        }
        return null;
    }

    private ProcessorContext createContext() {
        return new ProcessorContext(this.message);
    }
}
