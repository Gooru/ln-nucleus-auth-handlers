
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On 20-Nov-2018
 */
@Table("role_permission_mapping")
@CompositePK({"role_id", "permission_id"})
public class AJEntityRolePermissionMapping extends Model {

  public final static String ROLE_ID = "role_id";
  public final static String CODE = "code";

  public static final String PERMISSIONS_RESP_KEY = "permissions";
 
  public final static String FETCH_PERMISSIONS_BY_MULTIPLE_ROLES =
      "SELECT distinct p.code  FROM role_permission_mapping rp INNER JOIN permission p ON p.id = rp.permission_id WHERE role_id =  ANY(?::int[])";

}
 