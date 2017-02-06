package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilderFactory;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 *         Created On: 03-Jan-2017
 */
class SignoutUserProcessor extends AbstractCommandProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(SignoutUserProcessor.class);

    SignoutUserProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("processing user signout");
            RedisClient redisClient = RedisClient.instance();
            JsonObject tokenDetails = redisClient.getJsonObject(context.accessToken());
            if (tokenDetails != null) {
                String userId = tokenDetails.getString(ParameterConstants.PARAM_USER_ID);
                RedisClient.instance().del(context.accessToken());
                return MessageResponseFactory
                    .createNoContentResponse(EventBuilderFactory.getSignoutUserEventBuilder(userId));
            }
            return MessageResponseFactory.createNoContentResponse();
        } catch (Throwable t) {
            LOGGER.error("exception while user signout", t.getMessage());
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

}
