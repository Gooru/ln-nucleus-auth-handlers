package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("tenant")
public class AJEntityTenant extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityTenant.class);

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
  public static final String SHORT_NAME = "short_name";
  public static final String LOGIN_URL = "login_url";
  public static final String IMAGE_URL = "image_url";
  public static final String NAME = "name";
  public static final String TENANT_NAME = "tenant_name";
  public static final String TENANT_IMAGE_URL = "tenant_image_url";
  public static final String TENANT_SHORT_NAME = "tenant_short_name";

  public static final String GRANT_TYPE_OAUTH2 = "oauth2";
  public static final String GRANT_TYPE_GOOGLE = "google";
  public static final String GRANT_TYPE_CREDENTIAL = "credential";

  public static final String SELECT_BY_ID =
      "SELECT id, cdn_urls, access_token_validity, short_name FROM tenant WHERE id = ?::uuid AND status = 'active'";

  public static final String SELECT_BY_ID_SECRET =
      "SELECT id, cdn_urls, access_token_validity, short_name FROM tenant WHERE id = ?::uuid AND secret = ? AND grant_types @> "
          + "ARRAY[?]::text[] AND status = 'active'";

  public static final String SELECT_PARENT_TENANT =
      "SELECT parent_tenant FROM tenant WHERE id = ?::uuid AND status = 'active'";

  public static final String SELECT_BY_ID_GRANT_TYPE =
      "SELECT id, cdn_urls, access_token_validity, short_name FROM tenant WHERE id = ?::uuid  AND grant_types @> "
          + "ARRAY[?]::text[] AND status = 'active'";

  public static final String SELECT_BY_SHORT_NAME_GRANT_TYPE =
      "SELECT id, grant_types FROM tenant WHERE short_name = ?::varchar AND status = 'active'";

  public static final String SELECT_LOGIN_URL =
      "SELECT login_url FROM tenant where id = ?::uuid AND status = 'active'";

  public List<String> getGrantTypes() {
    Object grantTypes = this.get(GRANT_TYPES);
    if (grantTypes == null) {
      return null;
    }

    if (grantTypes instanceof java.sql.Array) {
      String[] result = new String[0];
      try {
        result = (String[]) ((java.sql.Array) grantTypes).getArray();
        List<String> types = new ArrayList<>();
        if (result.length == 0) {
          return types;
        }
        Collections.addAll(types, result);
        return types;
      } catch (SQLException e) {
        LOGGER.warn("Invalid grant types", e);
        return null;
      }
    } else {
      LOGGER.warn("grant type is not instance of array");
      return null;
    }
  }

}
