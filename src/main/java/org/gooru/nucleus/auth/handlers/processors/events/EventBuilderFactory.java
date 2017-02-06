package org.gooru.nucleus.auth.handlers.processors.events;

import org.gooru.nucleus.auth.handlers.processors.emails.EmailNotificationBuilder;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

    private static final String EVT_ANONYMOUS_SIGNIN = "event.anonymous.signin";
    private static final String EVT_USER_SIGNIN = "event.user.signin";
    private static final String EVT_USER_SIGNOUT = "event.user.signout";
    private static final String EVT_USER_TOKEN_CHECK = "event.user.token.check";
    private static final String EVT_USER_TOKEN_DETAILS = "event.user.token.details";
    private static final String EVT_USER_AUTHORIZE = "event.user.authorize";
    private static final String EVT_USER_SIGNUP = "event.user.signup";
    private static final String EVT_USER_UPDATE = "event.user.update";
    private static final String EVT_USER_PASSWORD_RESET_TRIGGER = "event.user.password.reset.trigger";
    private static final String EVT_USER_PASSWORD_RESET = "event.user.password.reset";
    private static final String EVT_USER_PASSWORD_CHANGE = "event.user.password.change";
    private static final String EVT_INTERNAL_AUTHENTICATE = "event.internal.authenticate";
    private static final String EVT_INTERNAL_IMPERSONATE = "event.internal.impersonate";
    private static final String EVT_INTERNAL_LTI_SSO = "event.internal.lti.sso";

    private static final String EVENT_NAME = "event.name";
    private static final String EVENT_BODY = "event.body";
    private static final String ID = "id";
    private static final String EMAIL_CONTEXT = "email_context";

    private EventBuilderFactory() {
        throw new AssertionError();
    }

    public static EventBuilder getAnonymousSigninEventBuilder(String clientId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_ANONYMOUS_SIGNIN)
            .put(EVENT_BODY, new JsonObject().put(ID, clientId));
    }

    public static EventBuilder getSigninUserEventBuilder(String userId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_SIGNIN)
            .put(EVENT_BODY, new JsonObject().put(ID, userId));
    }

    public static EventBuilder getSignupUserEventBuilder(String userId, EmailNotificationBuilder emailNotification) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_SIGNUP)
            .put(EVENT_BODY, new JsonObject().put(ID, userId).put(EMAIL_CONTEXT, emailNotification.build()));
    }

    public static EventBuilder getSignoutUserEventBuilder(String userId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_SIGNOUT)
            .put(EVENT_BODY, new JsonObject().put(ID, userId));
    }

    public static EventBuilder getAuthorizeUserEventBuilder(String userId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_AUTHORIZE)
            .put(EVENT_BODY, new JsonObject().put(ID, userId));
    }

    public static EventBuilder getUpdateUserEventBuilder(String userId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_UPDATE)
            .put(EVENT_BODY, new JsonObject().put(ID, userId));
    }

    public static EventBuilder getLTISSOEventBuilder(String userId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_INTERNAL_LTI_SSO)
            .put(EVENT_BODY, new JsonObject().put(ID, userId));
    }

    public static EventBuilder geTriggerResetPasswordEventBuilder(String userId,
        EmailNotificationBuilder emailNotification) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_PASSWORD_RESET_TRIGGER)
            .put(EVENT_BODY, new JsonObject().put(ID, userId).put(EMAIL_CONTEXT, emailNotification.build()));
    }

    public static EventBuilder getResetPasswordEventBuilder(String userId, EmailNotificationBuilder emailNotification) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_PASSWORD_RESET)
            .put(EVENT_BODY, new JsonObject().put(ID, userId).put(EMAIL_CONTEXT, emailNotification.build()));
    }

    public static EventBuilder getChangePasswordEventBuilder(String userId,
        EmailNotificationBuilder emailNotification) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_USER_PASSWORD_CHANGE)
            .put(EVENT_BODY, new JsonObject().put(ID, userId).put(EMAIL_CONTEXT, emailNotification.build()));
    }
}
