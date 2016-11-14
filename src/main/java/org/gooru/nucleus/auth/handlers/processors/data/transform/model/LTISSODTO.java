package org.gooru.nucleus.auth.handlers.processors.data.transform.model;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

import io.vertx.core.json.JsonObject;

public class LTISSODTO {

    private JsonObject requestBody;

    public LTISSODTO(JsonObject requestBody) {
        this.requestBody = requestBody;
    }
    
    public String getClientId() {
        return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_ID);
    }

    public String getClientKey() {
        return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_KEY);
    }

    public UserDTO getUser() {
        return new UserDTO(this.requestBody.getJsonObject(ParameterConstants.PARAM_USER));
    }
}
