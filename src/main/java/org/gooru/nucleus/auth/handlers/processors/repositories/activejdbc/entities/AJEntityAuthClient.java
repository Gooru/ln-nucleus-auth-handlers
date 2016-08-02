package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.AUTH_CLIENT)
@IdName(SchemaConstants.CLIENT_ID)
public class AJEntityAuthClient extends Model {

    public static final String GET_AUTH_CLIENT_ID_AND_KEY = "client_id = ?::uuid and client_key = ?";
    public static final String GET_AUTH_CLIENT_KEY = "client_key = ?";

    public static final String INSERT_AUTH_CLIENT =
        "INSERT INTO auth_client(client_id, name, url, client_key, description, contact_email, access_token_validity,"
            + " created_at, grant_types, cdn_urls) VALUES(?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)";
    
    public static final String SELECT_CLIENT_UUID = "SELECT gen_random_uuid() AS uuid";
    public static final String UUID = "uuid";
    public static final String CLIENT_KEY_SEPARATOR = "#";
    
    public String getClientId() {
        return getString(ParameterConstants.PARAM_CLIENT_ID);
    }

    public String getClientKey() {
        return getString(ParameterConstants.PARAM_CLIENT_KEY);
    }

    public JsonArray getGrantTypes() {
        return new JsonArray(getString(ParameterConstants.PARAM_GRANT_TYPES));
    }

    public JsonArray getRefererDomains() {
        String json = getString(ParameterConstants.PARAM_REFERER_DOMAINS);
        JsonArray refererDomains = null;
        if (json != null) {
            refererDomains = new JsonArray(json);
        }
        return refererDomains;
    }

    public int getAccessTokenValidity() {
        return getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
    }

    public JsonObject getCdnUrls() {
        String json = getString(ParameterConstants.PARAM_CDN_URLS);
        JsonObject cdnUrls = null;
        if (json != null) {
            cdnUrls = new JsonObject(json);
        }
        return cdnUrls;
    }
}
