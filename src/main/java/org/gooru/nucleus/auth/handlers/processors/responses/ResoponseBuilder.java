package org.gooru.nucleus.auth.handlers.processors.responses;

import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers.DBHelper;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityPartner;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 *         Created On: 03-Feb-2017
 */
public class ResoponseBuilder {

    private final ProcessorContext context;
    private final AJEntityUsers user;
    private final AJEntityTenant tenant;
    private final AJEntityPartner partner;
    private final RedisClient redisClient;

    public ResoponseBuilder(ProcessorContext context, AJEntityUsers user, AJEntityTenant tenant,
        AJEntityPartner partner) {
        this.context = context;
        this.user = user;
        this.tenant = tenant;
        this.partner = partner;
        this.redisClient = RedisClient.instance();
    }

    public JsonObject build() {
        final JsonObject result = new JsonObject();
        result.put(ParameterConstants.PARAM_USER_ID, user.getString(AJEntityUsers.ID));
        result.put(ParameterConstants.PARAM_APP_ID,
            context.requestBody().getString(ParameterConstants.PARAM_APP_ID, null));
        result
            .put(ParameterConstants.PARAM_PARTNER_ID, (partner != null) ? partner.getString(AJEntityPartner.ID) : null);
        result.put(AJEntityUsers.USERNAME, user.getString(AJEntityUsers.USERNAME));
        result.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        result.put(AJEntityUsers.EMAIL, user.getString(AJEntityUsers.EMAIL));
        result.put(ParameterConstants.PARAM_CDN_URLS, new JsonObject(tenant.getString(AJEntityTenant.CDN_URLS)));

        JsonObject tenantJson = new JsonObject();
        tenantJson.put(AJEntityUsers.TENANT_ID, tenant.getString(AJEntityTenant.ID));
        tenantJson.put(AJEntityUsers.TENANT_ROOT, user.getString(AJEntityUsers.TENANT_ROOT));
        result.put(ParameterConstants.PARAM_TENANT, tenantJson);

        JsonObject userPreference = DBHelper.getUserPreference(user.getString(AJEntityUsers.ID));
        result.put(AJEntityUserPreference.PREFERENCE_SETTINGS, userPreference);

        int accessTokenValidity = (partner != null) ? partner.getInteger(AJEntityPartner.ACCESS_TOKEN_VALIDITY) :
            tenant.getInteger(AJEntityTenant.ACCESS_TOKEN_VALIDITY);
        String partnerId = (partner != null) ? partner.getString(AJEntityPartner.ID) : null;
        final String token = InternalHelper
            .generateToken(user.getString(AJEntityUsers.ID), partnerId, tenant.getString(AJEntityTenant.ID));
        saveAccessToken(token, result, accessTokenValidity);

        result.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        result.put(AJEntityUsers.FIRST_NAME, user.getString(AJEntityUsers.FIRST_NAME));
        result.put(AJEntityUsers.LAST_NAME, user.getString(AJEntityUsers.LAST_NAME));
        result.put(AJEntityUsers.USER_CATEGORY, user.getString(AJEntityUsers.USER_CATEGORY));
        result.put(AJEntityUsers.THUMBNAIL, user.getString(AJEntityUsers.THUMBNAIL));

        return result;
    }

    private void saveAccessToken(String token, JsonObject session, Integer expireAtInSeconds) {
        session.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, session.toString(), expireAtInSeconds);
    }
}
