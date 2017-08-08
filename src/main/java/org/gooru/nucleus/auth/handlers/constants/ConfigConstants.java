package org.gooru.nucleus.auth.handlers.constants;

/**
 * Constant definition that are used to read configuration
 */
public final class ConfigConstants {

    private ConfigConstants() {
        throw new AssertionError();
    }

    public static final String PORT = "port";
    public static final String HOST = "host";
    public static final String REDIS = "redis";
}
