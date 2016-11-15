package org.gooru.nucleus.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

public class UserDTO {

    private JsonObject requestBody;

    public UserDTO(JsonObject requestBody) {
        this.requestBody = requestBody;
    }

    public String getFirstname() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    }

    public String getLastname() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_LASTNAME);
    }

    public String getUsername() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_USERNAME);
    }

    public String getEmailId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    }

    public String getUserCategory() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_CATEGORY);
    }

    public String getBirthDate() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_BIRTH_DATE);
    }

    public String getParentEmailId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_PARENT_EMAIL_ID);
    }

    public String getPassword() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_PASSWORD);
    }

    public String getSchoolId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_SCHOOL_ID);
    }

    public String getSchool() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_SCHOOL);
    }

    public String getSchoolDistrictId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
    }

    public String getSchoolDistrict() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
    }

    public String getStateId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_STATE_ID);
    }

    public String getState() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_STATE);
    }

    public String getCountryId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_COUNTRY_ID);
    }

    public String getCountry() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_COUNTRY);
    }

    public String getThumbnailPath() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_THUMBNAIL_PATH);
    }

    public String getGender() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_GENDER);
    }

    public JsonArray getGrade() {
        return this.requestBody.getJsonArray(ParameterConstants.PARAM_GRADE);
    }

    public JsonArray getCourse() {
        return this.requestBody.getJsonArray(ParameterConstants.PARAM_COURSE);
    }

    public String getIdentityId() {
        return this.requestBody.getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
    }

    public String getAboutMe() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_ABOUT_ME);
    }
    
    public String getRosterGlobalUserId() { 
        return this.requestBody.getString(ParameterConstants.PARAM_ROSTER_GLOBAL_USER_ID);
    }
    
    public String getReferenceId() {
        return this.requestBody.getString(ParameterConstants.PARAM_USER_REFERENCE_ID);
    }
}
