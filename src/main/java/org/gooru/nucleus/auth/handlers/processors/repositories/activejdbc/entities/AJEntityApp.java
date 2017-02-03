package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("app")
public class AJEntityApp extends Model {

    public static final String TABLE = "app";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String DESCRIPTION = "description";
    public static final String email = "email";
}
