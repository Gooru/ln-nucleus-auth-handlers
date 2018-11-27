package org.gooru.nucleus.auth.handlers.constants;

/**
 * @author szgooru
 *         Created On: 02-Jan-2017
 */
public final class ParameterConstants {

    private ParameterConstants() {
        throw new AssertionError();
    }

    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_KEY = "client_key";
    public static final String PARAM_ANONYMOUS_TOKEN = "anonymous_token";
    public static final String PARAM_GRANT_TYPE = "grant_type";
    public static final String PARAM_ACCESS_TOKEN_VALIDITY = "access_token_validity";
    public static final String PARAM_PROVIDED_AT = "provided_at";
    public static final String PARAM_ACCESS_TOKEN = "access_token";
    public static final String PARAM_CDN_URLS = "cdn_urls";
    public static final String PARAM_TENANT_ID = "tenant_id";
    public static final String PARAM_TENANT = "tenant";
    public static final String PARAM_APP_ID = "app_id";
    public static final String PARAM_PARTNER_ID = "partner_id";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_OLD_PASSWORD = "old_password";
    public static final String PARAM_NEW_PASSWORD = "new_password";
    public static final String PARAM_IDENTITY_ID = "identity_id";
    public static final String PARAM_SHORT_NAME = "shortname";

    public static final String PARAM_USER = "user";
    public static final String PARAM_USER_ID = "user_id";

    //Email Notification Constants
    public static final String MAIL_TEMPLATE_NAME = "mail_template_name";
    public static final String MAIL_TEMPLATE_CONTEXT = "mail_template_context";
    public static final String MAIL_TO_ADDRESSES = "to_addresses";
    public static final String MAIL_TOKEN = "token";
    public static final String MAIL_USERNAME = "username";

}
