
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On 20-Nov-2018
 */
@Table("role_permission_mapping")
@CompositePK({ "role_id", "permission_name" })
public class AJEntityRolePermissionMapping extends Model {

	public final static String ROLE_ID = "role_id";
	public final static String PERMISSION_NAME = "permission_name";

	public static final String PERMISSIONS_RESP_KEY = "permissions";

	public final static String FETCH_PERMISSIONS_BY_ROLE = "SELECT permission_name FROM role_permission_mapping WHERE role_id = ?";
	public final static String FETCH_PERMISSIONS_BY_MULTIPLE_ROLES = "SELECT distinct permission_name FROM role_permission_mapping WHERE role_id = ANY(?::int[])";

}
