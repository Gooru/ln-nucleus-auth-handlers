package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserState;
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
        if (email == null || email.isEmpty()) {
            return null;
        }

        LazyList<AJEntityUsers> users;
        if (partnerId != null) {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_PARTNER_ID, email.toLowerCase(), partnerId);
        } else {
            users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, email.toLowerCase(), tenantId);
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

    public static JsonObject getUserClientState(String userId) {
        AJEntityUserState userState = AJEntityUserState.findById(UUID.fromString(userId));
        return userState != null ? userState.getClientState() : null;
    }
    
    public static void storeUserSystemStateForSignup(String userId) {
        AJEntityUserState userState = new AJEntityUserState();
        userState.setUserId(userId);
        userState.setSystemState(AJEntityUserState.WELCOME_EMAIL_SENT_STATE);
        LOGGER.info(userState.toInsert());
        if (!userState.insert()) {
            LOGGER.debug("unable to save welcome email state for user '{}'", userId);
            LOGGER.debug(userState.errors().toString());
        }
    }
}
