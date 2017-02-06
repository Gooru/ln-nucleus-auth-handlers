package org.gooru.nucleus.auth.handlers.processors;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 *         Created On: 30-Dec-2016
 */
public class ProcessorContext {

    private final Message<Object> message;
    private final JsonObject data;

    public ProcessorContext(Message<Object> message) {
        this.message = message;
        this.data = (JsonObject) message.body();
    }

    public JsonObject requestBody() {
        return data.getJsonObject(MessageConstants.MSG_HTTP_BODY);
    }

    public JsonObject requestParams() {
        return data.getJsonObject(MessageConstants.MSG_HTTP_PARAM);
    }

    public MultiMap headers() {
        return message.headers();
    }

    public String operation() {
        return headers().get(MessageConstants.MSG_HEADER_OP);
    }

    public String accessToken() {
        return headers().get(MessageConstants.MSG_HEADER_TOKEN);
    }

    public JsonObject user() {
        return data.getJsonObject(MessageConstants.MSG_USER_CONTEXT_HOLDER);
    }
}
