package org.gooru.nucleus.auth.handlers.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HelperConstants {

    public static final Map<String, String> USER_GENDER;

    public static final Map<String, String> USER_CATEGORY;

    public static final Map<String, String> SSO_CONNECT_GRANT_TYPES;

    public static final String LOCATION = "Location";

    public static final String USER_ENTITY_URI = "/users/";

    public static final List<String> USERS_JSON_FIELDS = Arrays.asList("course", "grade");

    public static final List<String> USERS_PREFS_JSON_FIELDS = Arrays.asList("user_id", "profile_visibility");

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HEADER_TOKEN = "Token ";

    public static final int EXPIRE_IN_SECONDS = 86400;
    
    public static final int EXPIRE_IN_SECONDS_ETUSERS = 86400 * 15;
    
    public static final String CREATE_APP_KEY = "create.app.key";
    
    public static final String CHAR_ENCODING_UTF8 = "UTF-8";

    static {
        Map<String, String> gender = new HashMap<>();
        gender.put("male", "male");
        gender.put("female", "female");
        gender.put("other", "not wise to share");
        USER_GENDER = Collections.unmodifiableMap(gender);
    }

    static {
        Map<String, String> gender = new HashMap<>();
        gender.put("teacher", "Teacher");
        gender.put("student", "Student");
        gender.put("parent", "Parent");
        gender.put("other", "Other");
        USER_CATEGORY = Collections.unmodifiableMap(gender);
    }

    static {
        Map<String, String> ssoGrantType = new HashMap<>();
        ssoGrantType.put("google", "grant type used to connect with google authentication");
        ssoGrantType.put("wsfed", "grant type used to connect with wsfed authentication");
        ssoGrantType.put("saml", "grant type used to connect with saml authentication");
        SSO_CONNECT_GRANT_TYPES = Collections.unmodifiableMap(ssoGrantType);
    }

    public enum UserIdentityProvisionType {
        GOOGLE("google"), WSFED("wsfed"), SAML("saml"), REGISTERED("registered");

        public final String type;

        UserIdentityProvisionType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public enum UserIdentityLoginType {
        GOOGLE("google"), WSFED("wsfed"), SAML("saml"), CREDENTIAL("credential");

        public final String type;

        UserIdentityLoginType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public enum UserIdentityStatus {
        ACTIVE("active"), DEACTIVED("deactived"), DELETED("deleted");

        public final String status;

        UserIdentityStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return this.status;
        }
    }

    public enum GrantType {
        ANONYMOUS("anonymous"), CREDENTIAL("credential"), GOOGLE("google"), WSFED("wsfed"), SAML("saml");

        public final String type;

        GrantType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    private HelperConstants() {
        throw new AssertionError();
    }

}
