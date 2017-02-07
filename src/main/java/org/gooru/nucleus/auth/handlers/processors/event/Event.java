package org.gooru.nucleus.auth.handlers.processors.event;

public enum Event {

    CREATE_USER("event.user.create"),
    UPDATE_USER("event.user.update"),
    UPDATE_USER_PREFS("event.user.prefs.update"),
    UPDATE_USER_EMAIL_CONFIRM("event.user.update.email.confirm"),
    AUTHORIZE_USER("event.user.authorize"),
    AUTHENTICATION_USER("event.user.authentication"),
    LTISSO_LAUNCH("event.ltisso.launch");

    private final String name;

    Event(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
