package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_IDENTITY)
@IdName(SchemaConstants.ID)
public class AJEntityUserIdentity extends Model {

    public static final String GET_BY_USERNAME_PASSWORD =
        "username = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
    public static final String GET_BY_EMAIL_PASSWORD =
        "email_id = ?  and password = ? and login_type = 'credential' and status != 'deleted'";
    public static final String GET_BY_EMAIL = "email_id = ? and status != 'deleted'";
    public static final String GET_BY_USERNAME = "username = ? and status != 'deleted'";
    public static final String GET_BY_REFERENCE = "reference_id = ? and status != 'deleted'";
    public static final String GET_BY_REFERENCE_AND_CLIENTID = "reference_id = ? and client_id = ?::uuid and status != 'deleted'";
    public static final String GET_BY_ID_PASSWORD = "user_id = ?::uuid and password = ? and status != 'deleted'";
    public static final String GET_BY_USER_ID = "user_id = ?::uuid and  status != 'deleted'";
    public static final String GET_BY_CANONICAL_USERNAME = "canonical_username = ? and status != 'deleted'";
    public static final String GET_BY_CANONICAL_USERNAME_PASSWORD =
        "canonical_username = ?  and password = ? and login_type = 'credential' and status != 'deleted'";

    
    public static final String LOGIN_TYPE_LTISSO = "ltisso";
    public static final String PROVISION_TYPE_LTISSO = "ltisso";
    
    public String getUsername() {
        return getString(ParameterConstants.PARAM_USER_USERNAME);
    }

    public void setUsername(String username) {
        set(ParameterConstants.PARAM_USER_USERNAME, username);
    }
    
    public void setCanonicalUsername(String canonicalUsername) {
        set(ParameterConstants.PARAM_USER_CANONICAL_USERNAME, canonicalUsername);
    }

    public String getPassword() {
        return getString(ParameterConstants.PARAM_USER_PASSWORD);
    }

    public void setPassword(String password) {
        set(ParameterConstants.PARAM_USER_PASSWORD, password);
    }

    public String getClientId() {
        return getString(ParameterConstants.PARAM_CLIENT_ID);
    }

    public void setClientId(String clientId) {
        set(ParameterConstants.PARAM_CLIENT_ID, UUID.fromString(clientId));
    }

    public String getLoginType() {
        return getString(ParameterConstants.PARAM_LOGIN_TYPE);
    }

    public void setLoginType(String loginType) {
        set(ParameterConstants.PARAM_LOGIN_TYPE, DBEnums.loginType(loginType));
    }

    public String getProvisionType() {
        return getString(ParameterConstants.PARAM_USER_PROVISION_TYPE);
    }

    public void setProvisionType(String provisionType) {
        set(ParameterConstants.PARAM_USER_PROVISION_TYPE, DBEnums.provisionType(provisionType));
    }

    public String getReferenceId() {
        return getString(ParameterConstants.PARAM_USER_REFERENCE_ID);
    }

    public void setReferenceId(String referenceId) {
        set(ParameterConstants.PARAM_USER_REFERENCE_ID, referenceId);
    }

    public String getUserId() {
        return getString(ParameterConstants.PARAM_USER_ID);
    }

    public void setUserId(Object userId) {
        set(ParameterConstants.PARAM_USER_ID, userId);
    }

    public String getStatus() {
        return getString(ParameterConstants.PARAM_USER_STATUS);
    }

    public void setStatus(String status) {
        set(ParameterConstants.PARAM_USER_STATUS, DBEnums.userIdentityStatus(status));
    }

    public String getEmailId() {
        return getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    }

    public void setEmailId(String emailId) {
        set(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
    }

    public Boolean getEmailConfirmStatus() {
        return getBoolean(ParameterConstants.PARAM_USER_EMAIL_CONFIRM_STATUS);
    }

    public void setEmailConfirmStatus(Boolean value) {
        setBoolean(ParameterConstants.PARAM_USER_EMAIL_CONFIRM_STATUS, value);
    }

}
