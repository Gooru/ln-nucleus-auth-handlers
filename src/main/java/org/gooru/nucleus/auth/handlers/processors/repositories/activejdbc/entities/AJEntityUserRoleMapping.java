
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On 20-Nov-2018
 */
@Table("user_role_mapping")
@CompositePK({"role_id", "user_id"})
public class AJEntityUserRoleMapping extends Model {

  public final static String ROLE_ID = "role_id";
  public final static String USER_ID = "user_id";

  public final static String PERMISSIONS_RESP_KEY = "permissions";

  public static final String FETCH_USER_ROLE =
      "SELECT role_id FROM user_role_mapping WHERE user_id = ?::uuid";

  public final static String USERS = "users";

}
