package org.gooru.nucleus.auth.handlers.processors.emails;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 11-Jan-2017
 */
public final class EmailNotificationBuilder {
    private String templateName;

    private final JsonArray toAddresses;

    private final JsonArray ccAddresses;

    private final JsonObject context;

    public EmailNotificationBuilder() {
        this.toAddresses = new JsonArray();
        this.context = new JsonObject();
        this.ccAddresses = new JsonArray();
    }

    private JsonArray getToAddresses() {
        return toAddresses;
    }

    private JsonObject getContext() {
        return context;
    }

    private String getTemplateName() {
        return templateName;
    }

    public EmailNotificationBuilder addToAddress(String toAddress) {
        this.toAddresses.add(toAddress);
        return this;
    }

    public EmailNotificationBuilder addCcAddress(String ccAddress) {
        this.ccAddresses.add(ccAddress);
        return this;
    }

    public EmailNotificationBuilder putContext(String key, String value) {
        this.context.put(key, value);
        return this;
    }

    public EmailNotificationBuilder setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public JsonObject build() {
        JsonObject data = null;
        if (getTemplateName() != null) {
            data = new JsonObject();
            data.put(ParameterConstants.MAIL_TEMPLATE_NAME, getTemplateName());
            data.put(ParameterConstants.MAIL_TEMPLATE_CONTEXT, getContext());
            data.put(ParameterConstants.MAIL_TO_ADDRESSES, getToAddresses());
        }
        return data;
    }
}
