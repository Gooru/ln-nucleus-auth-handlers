package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("partner")
public class AJEntityPartner extends Model {

    public static final String TABLE = "partner";

    public static final String ID = "id";
    public static final String SECRET = "secret";
    public static final String TENANT_ID = "tenant_id";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String DESCRIPTION = "description";
    public static final String ACCESS_TOKEN_VALIDITY = "access_token_validity";
    public static final String EMAIL = "email";
    
    public static final String SELECT_BY_ID_SECRET =
        "SELECT id, tenant_id, access_token_validity FROM partner WHERE id = ?::uuid AND secret = ?";
}
