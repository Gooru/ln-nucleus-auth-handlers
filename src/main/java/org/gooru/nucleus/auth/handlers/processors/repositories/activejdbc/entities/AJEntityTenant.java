package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("tenant")
public class AJEntityTenant extends Model {

    public static final String TABLE = "tenant";

    public static final String ID = "id";
    public static final String SECRET = "secret";
    public static final String GRANT_TYPES = "grant_types";
    public static final String TENANT_TYPE = "tenant_type";
    public static final String CONTENT_VISIBILITY = "content_visibility";
    public static final String CLASS_VISIBILITY = "class_visibility";
    public static final String USER_VISIBILITY = "user_visibility";
    public static final String PARENT_TENANT = "parent_tenant";
    public static final String CREATOR_SYSTEM = "creator_system";
    public static final String IS_VERIFIED = "is_verified";
    public static final String VERIFICATION_DATE = "verification_date";
    public static final String STATUS = "status";
    public static final String SUBDOMAIN = "subdomain";
    public static final String SCHOOL = "school";
    public static final String SCHOOL_DISTRICT = "school_district";
    public static final String CDN_URLS = "cdn_urls";
    public static final String ACCESS_TOKEN_VALIDITY = "access_token_validity";

    public static final String SELECT_BY_ID =
        "SELECT id, cdn_urls, access_token_validity FROM tenant WHERE id = ?::uuid AND status = 'active'";

    public static final String SELECT_BY_ID_SECRET =
        "SELECT id, cdn_urls, access_token_validity FROM tenant WHERE id = ?::uuid AND secret = ? AND grant_types @> ARRAY[?]::text[]"
        + " AND status = 'active'";

}
