package org.gooru.nucleus.auth.handlers.app.components;

import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.nucleus.auth.handlers.processors.utils.PreferenceSettingsUtil;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public final class UtilityManager implements Initializer, Finalizer {
    private static final UtilityManager ourInstance = new UtilityManager();

    public static UtilityManager getInstance() {
        return ourInstance;
    }

    private UtilityManager() {
    }

    @Override
    public void finalizeComponent() {

    }

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        PreferenceSettingsUtil.initialize();
    }

}
