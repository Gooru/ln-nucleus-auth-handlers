package org.gooru.nucleus.auth.handlers.bootstrap;

import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializers;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorBuilder;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 *         Created On: 30-Dec-2016
 */
public class AuthHandlerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthHandlerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        vertx.executeBlocking(blockingFuture -> {
            startApplication();
            blockingFuture.complete();
        }, startApplicationFuture -> {
            if (startApplicationFuture.succeeded()) {
                final EventBus eb = vertx.eventBus();
                eb.consumer(MessagebusEndpoints.MBEP_AUTH_HANDLER, message -> {
                    LOGGER.debug("Received message: " + message.body());
                    vertx.executeBlocking(future -> {
                        MessageResponse response = new ProcessorBuilder(message).build().process();
                        future.complete(response);
                    }, res -> {
                        MessageResponse response = (MessageResponse) res.result();
                        message.reply(response.reply(), response.deliveryOptions());

                        JsonObject eventData = response.event();
                        if (eventData != null) {
                            LOGGER.debug("event data to be posted: {}", eventData.toString());
                            final String accessToken = getAccessToken(message, response);
                            eventData.put(MessageConstants.MSG_HEADER_SEESION_TOKEN,
                                accessToken.substring(MessageConstants.TOKEN.length()).trim());
                            InternalHelper.executeHTTPClientPost(eventData.toString(), accessToken);
                        }
                    });
                }).completionHandler(result -> {
                    if (result.succeeded()) {
                        LOGGER.info("Auth handlers end point ready to listen");
                        startFuture.complete();
                    } else {
                        LOGGER.error("Error registering the auth handlers. Halting the auth handlers machinery");
                        Runtime.getRuntime().halt(1);
                        startFuture.fail(result.cause());
                    }
                });
            } else {
                startFuture.fail("Not able to initialize the Auth Handlers machinery properly");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        shutDownApplication();
        super.stop();
    }

    private void startApplication() {
        Initializers initializers = new Initializers();
        try {
            for (Initializer initializer : initializers) {
                initializer.initializeComponent(vertx, config());
            }
        } catch (IllegalStateException ie) {
            LOGGER.error("Error initializing application", ie);
            Runtime.getRuntime().halt(1);
        }
    }

    private void shutDownApplication() {
        Finalizers finalizers = new Finalizers();
        for (Finalizer finalizer : finalizers) {
            finalizer.finalizeComponent();
        }
    }

    private String getAccessToken(Message<?> message, MessageResponse messageResponse) {
        String accessToken = ((JsonObject) message.body()).getString(MessageConstants.MSG_HEADER_TOKEN);
        if (accessToken == null || accessToken.isEmpty()) {
            final JsonObject result = messageResponse.reply();
            final JsonObject resultHttpBody = result.getJsonObject(MessageConstants.MSG_HTTP_BODY);
            final JsonObject resultHttpRes = resultHttpBody.getJsonObject(MessageConstants.MSG_HTTP_RESPONSE);
            if (resultHttpRes != null) {
                accessToken = resultHttpRes.getString(ParameterConstants.PARAM_ACCESS_TOKEN);
            }
        }
        return (HelperConstants.HEADER_TOKEN + accessToken);
    }
}
