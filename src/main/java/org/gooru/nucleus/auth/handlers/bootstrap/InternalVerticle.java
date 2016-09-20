package org.gooru.nucleus.auth.handlers.bootstrap;

import org.gooru.nucleus.auth.handlers.constants.MessagebusEndpoints;
import org.gooru.nucleus.auth.handlers.processors.ProcessorBuilder;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.ProcessorHandlerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class InternalVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalVerticle.class);
    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final EventBus eb = vertx.eventBus();
        eb.consumer(MessagebusEndpoints.MBEP_INTERNAL, message -> {
            LOGGER.debug("Received message: " + message.body());
            vertx.executeBlocking(future -> {
                MessageResponse result = new ProcessorBuilder(ProcessorHandlerType.INTERNAL, message).build().process();
                future.complete(result);
            }, res -> {
                MessageResponse result = (MessageResponse) res.result();
                message.reply(result.reply(), result.deliveryOptions());
            });
        }).completionHandler(result -> {
            if (result.succeeded()) {
                LOGGER.info("Internal end point ready to listen");
                startFuture.complete();
            } else {
                LOGGER.error("Error registering the internal handler. Halting the internal machinery");
                startFuture.fail(result.cause());
            }
        });
    }
}