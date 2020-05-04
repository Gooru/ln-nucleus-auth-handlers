package org.gooru.nucleus.auth.handlers.app.components;

import java.util.ResourceBundle;
import org.gooru.nucleus.auth.handlers.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 16-May-2017
 */
public class AppConfiguration implements Initializer, Finalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);
  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);
  private static final AppConfiguration INSTANCE = new AppConfiguration();

  private static final String KEY_APP_CONFIG = "app.config";
  private static final String KEY_APPID_REQUIRED = "appid.required";
  private static final String KEY_CREDENTIAL_APPLOGIN_URL = "credential.applogin.url";
  private static final String KEY_GOOGLE_APPLOGIN_URL = "google.applogin.url";
  private static final String KEY_OAUTH2_APPLOGIN_URL = "oauth2.applogin.url";
  private static final String KEY_WSFED_APPLOGIN_URL = "wsfed.applogin.url";
  private static final String KEY_DEFAULT_APPLOGIN_URL = "default.applogin.url";
  private static final String KEY_REFRESH_TOKEN_EXPIRE_IN_SECS = "refresh.token.expire.in.secs";

  private Boolean isAppIdRequired;

  private String credentialAppLoginUrl;
  private String googleAppLoginUrl;
  private String oauth2AppLoginUrl;
  private String defaultLoginUrl;
  private String wsfedAppLoginUrl;
  private Integer refreshTokenExpireInSecs;

  private AppConfiguration() {}

  public static AppConfiguration getInstance() {
    return INSTANCE;
  }

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {

    JsonObject appConfig = config.getJsonObject(KEY_APP_CONFIG);
    if (appConfig == null || appConfig.isEmpty()) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("app.config.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("app.config.not.found"));
    }

    this.isAppIdRequired = appConfig.getBoolean(KEY_APPID_REQUIRED);
    if (this.isAppIdRequired == null) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("appid.required.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("appid.required.not.found"));
    }
    this.credentialAppLoginUrl = appConfig.getString(KEY_CREDENTIAL_APPLOGIN_URL);
    if (this.credentialAppLoginUrl == null) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("credential.login.url.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("credential.login.url.not.found"));
    }

    this.googleAppLoginUrl = appConfig.getString(KEY_GOOGLE_APPLOGIN_URL);
    if (this.googleAppLoginUrl == null || this.googleAppLoginUrl.isEmpty()) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("google.login.url.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("google.login.url.not.found"));
    }

    this.oauth2AppLoginUrl = appConfig.getString(KEY_OAUTH2_APPLOGIN_URL);
    if (this.oauth2AppLoginUrl == null || this.oauth2AppLoginUrl.isEmpty()) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("oauth2.login.url.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("oauth2.login.url.not.found"));
    }
    
    this.wsfedAppLoginUrl = appConfig.getString(KEY_WSFED_APPLOGIN_URL);
    if (this.wsfedAppLoginUrl == null || this.wsfedAppLoginUrl.isEmpty()) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("wsfed.login.url.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("wsfed.login.url.not.found"));
    }

    this.defaultLoginUrl = appConfig.getString(KEY_DEFAULT_APPLOGIN_URL);
    if (this.defaultLoginUrl == null || this.defaultLoginUrl.isEmpty()) {
      LOGGER.warn(RESOURCE_BUNDLE.getString("default.login.url.not.found"));
      throw new AssertionError(RESOURCE_BUNDLE.getString("default.login.url.not.found"));
    }
    this.refreshTokenExpireInSecs =
        appConfig.getInteger(KEY_REFRESH_TOKEN_EXPIRE_IN_SECS, 31536000);

    LOGGER.debug("AppID required flag set to:{}", isAppIdRequired);
    LOGGER.debug("App Configuration component initialized successfully");
  }

  @Override
  public void finalizeComponent() {}

  public boolean isAppIdRequired() {
    return isAppIdRequired;
  }

  public String credentialAppLoginUrl() {
    return credentialAppLoginUrl;
  }

  public String googleAppLoginUrl() {
    return googleAppLoginUrl;
  }

  public String oauth2AppLoginUrl() {
    return oauth2AppLoginUrl;
  }
  
  public String wsfedAppLoginUrl() {
    return wsfedAppLoginUrl;
  }

  public String defaultLoginUrl() {
    return defaultLoginUrl;
  }

  public Integer refreshTokenExpireInSecs() {
    return refreshTokenExpireInSecs;
  }
}
