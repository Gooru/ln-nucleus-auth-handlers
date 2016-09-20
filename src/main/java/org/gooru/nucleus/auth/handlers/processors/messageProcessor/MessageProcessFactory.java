package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageProcessFactory {

    private static final Map<ProcessorHandlerType, MessageProcessorHandler> instances = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessFactory.class);

    public static MessageProcessorHandler getInstance(ProcessorHandlerType handlerType) {
        MessageProcessorHandler handler = instances.get(handlerType);
        if (handler == null) {
            synchronized (MessageProcessFactory.class) {
                if (handlerType.equals(ProcessorHandlerType.AUTHENTICATION)) {
                    handler = new AuthenticationMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.AUTHORIZE)) {
                    handler = new AuthorizeMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.USER)) {
                    handler = new UserMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.USER_PREFS)) {
                    handler = new UserPrefsMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.AUTHENTICATION_GLA_VERSION)) {
                    handler = new AuthenticationGLAVersionMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.AUTH_CLIENT)) {
                    handler = new AuthClientMessageProcessor();
                } else if (handlerType.equals(ProcessorHandlerType.INTERNAL)) {
                    handler = new InternalMessageProcessor();
                } else {
                    LOG.debug("None of the handlers matched, looks like invalid handler type.");
                }
            }
            instances.put(handlerType, handler);
        }

        return handler;

    }
}
