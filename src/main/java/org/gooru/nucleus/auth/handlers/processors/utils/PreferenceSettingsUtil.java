package org.gooru.nucleus.auth.handlers.processors.utils;

import org.gooru.nucleus.auth.handlers.app.components.DataSourceRegistry;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 01-Feb-2017
 */
public final class PreferenceSettingsUtil {

  private static JsonObject defaultPreference;
  private static final String DEFAULT_PREF_LOOKUP_KEY = "DEFAULT_PREFERENCE";

  private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceSettingsUtil.class);
  private static final String SELECT_DEFAULT_PREF =
      "SELECT value FROM default_lookup WHERE key = ?";

  private PreferenceSettingsUtil() {
    throw new AssertionError();
  }

  public static JsonObject getDefaultPreference() {
    if (defaultPreference == null) {
      LOGGER.warn("returning null default preference");
    }

    return defaultPreference;
  }

  public static void initialize() {
    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      Object result = Base.firstCell(SELECT_DEFAULT_PREF, DEFAULT_PREF_LOOKUP_KEY);
      if (result == null) {
        throw new AssertionError("Default preference not found");
      }
      defaultPreference = new JsonObject(result.toString());
    } catch (Throwable e) {
      LOGGER.error("Caught exception while fetching default preference", e);
      throw new IllegalStateException(e);
    } finally {
      Base.close();
    }
  }
}
