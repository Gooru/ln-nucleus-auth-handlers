package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.gooru.nucleus.auth.handlers.processors.utils.PreferenceSettingsUtil;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 * Created On: 07-Jan-2017
 */
public final class DBHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);

    private DBHelper() {
        throw new AssertionError();
    }
    
    public static AJEntityPartner getPartnerByIDAndSecret(String partnerId, String secret) {
        LazyList<AJEntityPartner> partners = AJEntityPartner.findBySQL(AJEntityPartner.SELECT_BY_ID_SECRET, partnerId,
            InternalHelper.encryptClientKey(secret));
        return partners.isEmpty() ? null : partners.get(0);
    }
    
    public static AJEntityTenant getTenantByIdSecretGrantType(String tenantId, String secret, String grantType) {
        LazyList<AJEntityTenant> tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID_SECRET, tenantId,
            InternalHelper.encryptClientKey(secret), grantType);
        return tenants.isEmpty() ? null : tenants.get(0);
    }
    
    public static AJEntityTenant getTenantById(String tenantId) {
        LazyList<AJEntityTenant> tenants =
            AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_ID, tenantId);
        return tenants.isEmpty() ? null : tenants.get(0);
    }
    
    public static AJEntityUsers getUserByIdAndTenantId(String userId, String tenantId) {
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID_TENANT_ID, userId, tenantId);
        return users.isEmpty() ? null : users.get(0);
    }
    
    public static AJEntityUsers getUserById(String userId) {
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_ID, userId);
        return users.isEmpty() ? null : users.get(0);
    }
    
    public static AJEntityUsers getUserByEmail(String email) {
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL, email);
        return users.isEmpty() ? null : users.get(0);
    }
    
    public static AJEntityUsers getUserByEmailAndTenantId(String email, String tenantId) {
        LazyList<AJEntityUsers> users = AJEntityUsers.findBySQL(AJEntityUsers.SELECT_BY_EMAIL_TENANT_ID, email, tenantId);
        return users.isEmpty() ? null : users.get(0);
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
