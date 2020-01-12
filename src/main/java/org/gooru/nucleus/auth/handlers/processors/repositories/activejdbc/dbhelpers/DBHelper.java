package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityRolePermissionMapping;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserRoleMapping;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserState;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 07-Jan-2017
 */
public final class DBHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);

  private DBHelper() {
    throw new AssertionError();
  }

  public static AJEntityUsers getUserByIdAndTenantId(String userId, String tenantId) {
    LazyList<AJEntityUsers> users =
        AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_TENANT_ID, userId, tenantId);
    return users.isEmpty() ? null : users.get(0);
  }

  public static AJEntityUsers getUserByIdAndPartnerId(String userId, String partnerId) {
    LazyList<AJEntityUsers> users =
        AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_PARTNER_ID, userId, partnerId);
    return users.isEmpty() ? null : users.get(0);
  }

  public static AJEntityUsers getUserById(String userId) {
    LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID, userId);
    return users.isEmpty() ? null : users.get(0);
  }

  public static AJEntityUsers getUserByEmailAndTenantId(String email, String tenantId,
      String partnerId) {
    if (email == null || email.isEmpty()) {
      return null;
    }

    LazyList<AJEntityUsers> users;
    if (partnerId != null) {
      users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_PARTNER_ID, email.toLowerCase(),
          partnerId);
    } else {
      users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, email.toLowerCase(),
          tenantId);
    }
    return users.isEmpty() ? null : users.get(0);
  }

  public static AJEntityUsers getUserByUsername(String username, String tenantId, String partnerId,
      boolean isPartner) {
    if (username == null || username.isEmpty()) {
      return null;
    }

    AJEntityUsers user;
    if (isPartner) {
      user = AJEntityUsers.findFirst(AJEntityUsers.SELECT_BY_USERNAME_PARTNER_ID,
          username.toLowerCase(), partnerId);
    } else {
      user = AJEntityUsers.findFirst(AJEntityUsers.SELECT_BY_USERNAME_TENANT_ID,
          username.toLowerCase(), tenantId);
    }
    return user;
  }

  public static JsonObject getUserPreference(String userId, String tenantId) {
    AJEntityUserPreference userPreference =
        AJEntityUserPreference.findFirst(AJEntityUserPreference.SELECT_BY_USERID, userId);
    JsonObject userPreferenceJson;
    if (userPreference == null) {
      LOGGER.warn("user preferences not found, returning default");
      userPreferenceJson = TenantHelper.mergeDefaultPrefsWithTenantFrameworkPrefs(tenantId);
    } else {
      userPreferenceJson =
          new JsonObject(userPreference.getString(AJEntityUserPreference.PREFERENCE_SETTINGS));
    }

    return userPreferenceJson;
  }
  
  public static JsonObject getTenantSettings(String tenantId) {
    return TenantHelper.getTenantSettings(tenantId);
  }

  public static JsonObject getUserClientState(String userId) {
    AJEntityUserState userState = AJEntityUserState.findById(UUID.fromString(userId));
    return userState != null ? userState.getClientState() : null;
  }

  public static void storeUserSystemStateForSignup(String userId) {
    AJEntityUserState userState = new AJEntityUserState();
    userState.setUserId(userId);
    userState.setSystemState(AJEntityUserState.WELCOME_EMAIL_SENT_STATE);
    if (!userState.insert()) {
      LOGGER.debug("unable to save welcome email state for user '{}'", userId);
    }
  }

  public static JsonArray getUserRolesAndPermission(String userId) {
    LazyList<AJEntityUserRoleMapping> userRoles =
        AJEntityUserRoleMapping.findBySQL(AJEntityUserRoleMapping.FETCH_USER_ROLE, userId);
    if (userRoles.isEmpty()) {
      LOGGER.debug("No role has been assigned to user '{}'", userId);
      return null;
    }

    List<Integer> userRoleIds = new ArrayList<>();
    userRoles.forEach(role -> {
      userRoleIds.add(role.getInteger(AJEntityUserRoleMapping.ROLE_ID));
    });

    List<Map> rolePermissions = Base
        .findAll(AJEntityRolePermissionMapping.FETCH_PERMISSIONS_BY_MULTIPLE_ROLES,
            InternalHelper.toPostgresArrayInt(userRoleIds));
    JsonArray permissionsArray = new JsonArray();
    rolePermissions.forEach(mapping -> {
      permissionsArray.add(mapping.get(AJEntityRolePermissionMapping.CODE));
    });

    return permissionsArray;
  }
  
  public static String toPostgresArrayString(Collection<String> input) {
    int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
    // 36 chars
    Iterator<String> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder(approxSize);
    sb.append('{');
    for (; ; ) {
      String s = it.next();
      sb.append('"').append(s).append('"');
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',');
    }
  }
  
}
