package org.gooru.nucleus.auth.handlers.constants;

public class MessagebusEndpoints {
    /*
     * Any change here in end points should be done in the auth gateway side as
     * well, as both sender and receiver should be in sync
     */
    public static final String MBEP_AUTH = "org.gooru.nucleus.auth.message.bus.auth";
    public static final String MBEP_AUTHENTICATION = "org.gooru.nucleus.auth.message.bus.authentication";
    public static final String MBEP_GLA_VERSION_AUTHENTICATION =
        "org.gooru.nucleus.auth.message.bus.gla.version.authentication";
    public static final String MBEP_AUTHORIZE = "org.gooru.nucleus.auth.message.bus.authorize";
    public static final String MBEP_USER = "org.gooru.nucleus.auth.message.bus.user";
    public static final String MBEP_USER_PREFS = "org.gooru.nucleus.auth.message.bus.user.prefs";
    public static final String MBEP_EVENT = "org.gooru.nucleus.message.bus.publisher.event";
    public static final String MBEP_EMAIL_NOFIFY = "org.gooru.nucleus.auth.message.bus.email.notify";
    public static final String MBEP_AUTH_CLIENT = "org.gooru.nucleus.auth.message.bus.auth.client";
    public static final String MBEP_INTERNAL = "org.gooru.nucleus.auth.message.bus.internal";

}
