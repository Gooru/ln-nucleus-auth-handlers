package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.utils.PreferenceSettingsUtil;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_TENANT_ID, userId, tenantId);
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

    public static AJEntityUsers getUserByEmailAndTenantId(String email, String tenantId, String partnerId) {
        LazyList<AJEntityUsers> users;
        if (partnerId != null) {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_PARTNER_ID, email, partnerId);
        } else {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, email, tenantId);
        }
        return users.isEmpty() ? null : users.get(0);
    }

    public static AJEntityUsers getUserByUsername(String username, String tenantId, String partnerId,
        boolean isPartner) {
        AJEntityUsers user;
        if (isPartner) {
            user =
                AJEntityUsers.findFirst(AJEntityUsers.SELECT_BY_USERNAME_PARTNER_ID, username.toLowerCase(), partnerId);
        } else {
            user =
                AJEntityUsers.findFirst(AJEntityUsers.SELECT_BY_USERNAME_TENANT_ID, username.toLowerCase(), tenantId);
        }
        return user;
    }

    public static JsonObject getUserPreference(String userId) {
        AJEntityUserPreference userPreference =
            AJEntityUserPreference.findFirst(AJEntityUserPreference.SELECT_BY_USERID, userId);
        JsonObject userPreferenceJson;
        if (userPreference == null) {
            LOGGER.warn("user preferences not found, returning default");
            userPreferenceJson = PreferenceSettingsUtil.getDefaultPreference();
        } else {
            userPreferenceJson = new JsonObject(userPreference.getString(AJEntityUserPreference.PREFERENCE_SETTINGS));
        }

        return userPreferenceJson;
    }
}
