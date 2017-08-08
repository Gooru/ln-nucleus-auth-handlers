package org.gooru.nucleus.auth.handlers.processors.commands;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 *         Created On: 05-Jan-2017
 */
public class CheckAccessTokenProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAccessTokenProcessor.class);

    CheckAccessTokenProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        try {
            LOGGER.info("checking access token in redis");
            JsonObject accessTokenDetails = RedisClient.instance().getJsonObject(context.accessToken());
            if (accessTokenDetails != null && !accessTokenDetails.isEmpty()) {
                int expireAtInSeconds = accessTokenDetails.getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
                RedisClient.instance().expire(context.accessToken(), expireAtInSeconds);
                LOGGER.debug("access token found in redis. returing success");
                return MessageResponseFactory.createGetResponse(accessTokenDetails);
            } else {
                LOGGER.debug("access token not found in redis. returing 401 unauthorized");
                return MessageResponseFactory.createUnauthorizedResponse();
            }
        } catch (Throwable t) {
            LOGGER.error("exception while checking access token in redis", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

}
