package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.sql.SQLException;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 06-Dec-2017
 */
@Table("user_state")
@IdName("user_id")
public class AJEntityUserState extends Model {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityUserState.class);

    public static final String USER_ID = "user_id";
    public static final String SYSTEM_STATE = "system_state";
    public static final String CLIENT_STATE = "client_state";

    public static final String SELECT_CLIENT_STATE = "SELECT client_state FROM user_state WHERE user_id = ?::uuid";
    
    private static final String JSONB_TYPE = "jsonb";
    private static final String UUID_TYPE = "uuid";
    
    public static final String WELCOME_EMAIL_SENT_STATE = "{\"welcome.email.sent\" : false}";
    
    public JsonObject getClientState() {
        String clientState = this.getString(CLIENT_STATE);
        return clientState != null && !clientState.isEmpty() ? new JsonObject(clientState) : null;
    }
    
    public void setUserId(String userId) {
        setPGObject(USER_ID, UUID_TYPE, userId);
    }
    
    public void setSystemState(String systemState) {
        setPGObject(SYSTEM_STATE, JSONB_TYPE, systemState);
    }
    
    private void setPGObject(String field, String type, String value) {
        PGobject pgObject = new PGobject();
        pgObject.setType(type);
        try {
            pgObject.setValue(value);
            this.set(field, pgObject);
        } catch (SQLException e) {
            LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
            this.errors().put(field, value);
        }
    }
}