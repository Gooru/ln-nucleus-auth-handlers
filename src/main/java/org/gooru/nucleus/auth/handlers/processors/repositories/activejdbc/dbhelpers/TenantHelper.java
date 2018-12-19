package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import java.util.Set;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenantSetting;
import org.gooru.nucleus.auth.handlers.processors.utils.PreferenceSettingsUtil;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 31-Oct-2017
 */
public final class TenantHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantHelper.class);
  private static final String STANDARD_PREFERNCE = "standard_preference";

  private TenantHelper() {
    throw new AssertionError();
  }

  public static String getTenantRoot(String tenantId) {
    String tenantRoot = null;
    boolean isFound = false;
    final String origTenantId = tenantId;

    try {
      while (!isFound) {
        Object parentTenant = Base.firstCell(AJEntityTenant.SELECT_PARENT_TENANT, tenantId);
        if (parentTenant != null) {
          tenantId = parentTenant.toString();
          continue;
        }

        tenantRoot = tenantId;
        isFound = true;
      }
    } catch (Throwable t) {
      LOGGER.warn("unable to find tenant root of the tenant '{}'", tenantId);
    }

    return origTenantId.equalsIgnoreCase(tenantRoot) ? null : tenantRoot;
  }

  public static JsonObject mergeDefaultPrefsWithTenantFrameworkPrefs(String tenantId) {
    final JsonObject tenantFrameworkPrefs = getTenantFrameworkPrefs(tenantId);
    final JsonObject userPreference = PreferenceSettingsUtil.getDefaultPreference().copy();
    if (tenantFrameworkPrefs != null) {
      JsonObject userFrameworkPrefs = new JsonObject();
      Set<String> subjectIds = tenantFrameworkPrefs.getMap().keySet();
      subjectIds.forEach(subjectId -> {
        JsonObject subjectTenantFrameworkPrefs = tenantFrameworkPrefs.getJsonObject(subjectId);
        final String defaultSubjectFrameworkId =
            subjectTenantFrameworkPrefs.getString(AJEntityTenantSetting.DEFAULT_FW_ID);
        if (defaultSubjectFrameworkId != null) {
          userFrameworkPrefs.put(subjectId, defaultSubjectFrameworkId);
        }
      });
      userPreference.put(STANDARD_PREFERNCE, userFrameworkPrefs);
    }
    return userPreference;
  }

  private static JsonObject getTenantFrameworkPrefs(String tenantId) {
    JsonObject tenantFrameworkPrefs = null;
    AJEntityTenantSetting tenantSettings = AJEntityTenantSetting
        .first(AJEntityTenantSetting.SELECT_TENANT_SETTING_TX_FW_PREFS, tenantId);
    if (tenantSettings != null) {
      tenantFrameworkPrefs = new JsonObject(tenantSettings.getString(AJEntityTenantSetting.VALUE));
    }
    return tenantFrameworkPrefs;
  }
}
