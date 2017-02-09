package org.gooru.nucleus.auth.handlers.app.components;

import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 * Created On: 06-Feb-2017
 */
public final class AppHttpClient implements Initializer, Finalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppHttpClient.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);
    
    private static final AppHttpClient INSTANCE = new AppHttpClient();
    
    private static final String KEY_ENDPOINT = "api.endpoint";
    private static final String KEY_HOST = "api.host";
    private static final String KEY_EVENT_CONFIG = "event.config";
    
    private Vertx vertx;
    private String host;
    private String endpoint;
    
    private AppHttpClient() {
    }
    
    public static AppHttpClient getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        
        JsonObject eventConfig = config.getJsonObject(KEY_EVENT_CONFIG);
        if (eventConfig == null || eventConfig.isEmpty()) {
            LOGGER.warn(RESOURCE_BUNDLE.getString("event.config.not.found"));
            throw new AssertionError(RESOURCE_BUNDLE.getString("event.config.not.found"));
        }
        
        this.host = eventConfig.getString(KEY_HOST);
        if (this.host == null || this.host.isEmpty()) {
            LOGGER.warn(RESOURCE_BUNDLE.getString("api.host.missing"));
            throw new AssertionError(RESOURCE_BUNDLE.getString("api.host.missing"));
        }
        
        this.endpoint = eventConfig.getString(KEY_ENDPOINT);
        if (this.endpoint == null || this.endpoint.isEmpty()) {
            LOGGER.warn(RESOURCE_BUNDLE.getString("api.endpoint.missing"));
            throw new AssertionError(RESOURCE_BUNDLE.getString("api.endpoint.missing"));
        }
        LOGGER.debug("App Http Client initialized successfully");
    }
    
    public HttpClient getHttpClient() {
        return vertx.createHttpClient(new HttpClientOptions().setDefaultHost(this.host));
    }
    
    public String host() {
        return host;
    }

    public String endpoint() {
        return endpoint;
    }

    @Override
    public void finalizeComponent() {
    }
}
