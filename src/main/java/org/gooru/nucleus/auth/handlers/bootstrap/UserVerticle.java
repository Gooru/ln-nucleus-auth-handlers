package org.gooru.nucleus.auth.handlers.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.processors.ProcessorBuilder;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.ProcessorHandlerType;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(UserVerticle.class);

    @Override
    public void start(Future<Void> voidFuture) throws Exception {
        final EventBus eb = vertx.eventBus();
        final ConfigRegistry configRegistry = ConfigRegistry.instance();
        eb.consumer(MessagebusEndpoints.MBEP_USER, message -> {
            LOG.debug("Received message: " + message.body());
            vertx.executeBlocking(future -> {
                MessageResponse result = new ProcessorBuilder(ProcessorHandlerType.USER, message).build().process();
                future.complete(result);
            }, res -> {
                MessageResponse result = (MessageResponse) res.result();
                message.reply(result.reply(), result.deliveryOptions());

                JsonObject eventData = result.event();
                if (eventData != null) {
                    final String accessToken = getAccessToken(message, result);
                    InternalHelper
                        .executeHTTPClientPost(configRegistry.getEventRestApiUrl(), eventData.toString(), accessToken);
                }
                if (result.mailNotify() != null && result.mailNotify().size() > 0) {
                    final String accessToken = getAccessToken(message, result);
                    final JsonArray mailNotifies = result.mailNotify();
                    Stream<JsonObject> stream = mailNotifies.stream().map(mailNotify -> (JsonObject) mailNotify);
                    stream.forEach((JsonObject mailNotify) -> {
                        LOG.debug("sending email with data :{}", mailNotify.toString());
                        InternalHelper.executeHTTPClientPost(configRegistry.getMailRestApiUrl(), mailNotify.toString(),
                            accessToken);
                    });
                }
            });

        }).completionHandler(result -> {
            if (result.succeeded()) {
                LOG.info("User end point ready to listen");
                voidFuture.complete();
            } else {
                LOG.error("Error registering the user handler. Halting the user machinery");
                voidFuture.fail(result.cause());
            }
        });
    }

    private String getAccessToken(Message<?> message, MessageResponse messageResponse) {
        String accessToken = ((JsonObject) message.body()).getString(MessageConstants.MSG_HEADER_TOKEN);
        if (accessToken == null || accessToken.isEmpty()) {
            final JsonObject result = (JsonObject) messageResponse.reply();
            final JsonObject resultHttpBody = result.getJsonObject(MessageConstants.MSG_HTTP_BODY);
            final JsonObject resultHttpRes = resultHttpBody.getJsonObject(MessageConstants.MSG_HTTP_RESPONSE);
            if (resultHttpRes != null) {
                accessToken = resultHttpRes.getString(ParameterConstants.PARAM_ACCESS_TOKEN);
            }
        }
        return (HelperConstants.HEADER_TOKEN + accessToken);
    }

}
