
package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On 20-Nov-2018
 */
@Table("role")
public class AJEntityRole extends Model {

	public final static String ID = "id";
	public final static String NAME = "name";

	public static final List<String> FETCH_ROLE_FIELDS = Arrays.asList(ID, NAME);

	public static final String FETCH_ROLES = "SELECT id, name, description FROM role WHERE id = ANY(?::int[])";
}
