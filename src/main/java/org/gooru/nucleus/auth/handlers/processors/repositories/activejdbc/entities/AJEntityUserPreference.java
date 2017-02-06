package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 02-Feb-2017
 */
@Table("user_preference")
@IdName("user_id")
public class AJEntityUserPreference extends Model {

    public static final String USER_ID = "user_id";
    public static final String PREFERENCE_SETTINGS = "preference_settings";

    public static final String SELECT_BY_USERID = "user_id = ?::uuid";

    public static final List<String> RESPONSE_FIELDS = Arrays.asList(PREFERENCE_SETTINGS);

}
