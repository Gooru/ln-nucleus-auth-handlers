package org.gooru.nucleus.auth.handlers.constants;

/**
 * @author szgooru
 * Created On: 30-Dec-2016
 *
 */
public final class MessagebusEndpoints {

    private MessagebusEndpoints() {
        throw new AssertionError();
    }

    public static final String MBEP_AUTH_HANDLER = "org.gooru.nucleus.auth.message.bus.auth.handler";
}
