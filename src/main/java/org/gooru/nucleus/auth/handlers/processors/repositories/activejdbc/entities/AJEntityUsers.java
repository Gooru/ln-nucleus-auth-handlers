package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.time.format.DateTimeFormatter;
import java.util.*;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors.ConverterRegistry;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors.FieldConverter;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("users")
public class AJEntityUsers extends Model {

    public static final String TABLE = "users";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String REFERENCE_ID = "reference_id";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String LOGIN_TYPE = "login_type";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String PARENT_USER_ID = "parent_user_id";
    public static final String USER_CATEGORY = "user_category";
    public static final String ROLES = "roles";
    private static final String BIRTH_DATE = "birth_date";
    private static final String GRADE = "grade";
    public static final String COURSE = "course";
    public static final String THUMBNAIL = "thumbnail";
    private static final String GENDER = "gender";
    private static final String ABOUT = "about";
    public static final String SCHOOL_ID = "school_id";
    public static final String SCHOOL = "school";
    private static final String SCHOOL_DISTRICT_ID = "school_district_id";
    public static final String SCHOOL_DISTRICT = "school_district";
    private static final String COUNTRY_ID = "country_id";
    private static final String COUNTRY = "country";
    private static final String STATE_ID = "state_id";
    private static final String STATE = "state";
    public static final String METADATA = "metadata";
    public static final String ROSTER_ID = "roster_id";
    private static final String ROSTER_GLOBAL_USERID = "roster_global_userid";
    public static final String TENANT_ROOT = "tenant_root";
    public static final String TENANT_ID = "tenant_id";
    public static final String PARENT_ID = "partner_id";
    public static final String IS_DELETED = "is_deleted";

    private static final Set<String> CREATABLE_FIELDS = new HashSet<>(
        Arrays.asList(USERNAME, EMAIL, PASSWORD, BIRTH_DATE, FIRST_NAME, LAST_NAME, TENANT_ID, GENDER, USER_CATEGORY));

    private static final Set<String> MANDATORY_FIELDS =
        new HashSet<>(Arrays.asList(USERNAME, EMAIL, PASSWORD, BIRTH_DATE, FIRST_NAME, LAST_NAME, TENANT_ID));

    private static final Set<String> UPDATABLE_FIELDS = new HashSet<>(Arrays
        .asList(ABOUT, FIRST_NAME, LAST_NAME, COUNTRY, COUNTRY_ID, GRADE, ROSTER_GLOBAL_USERID, SCHOOL_DISTRICT_ID,
            STATE, STATE_ID, THUMBNAIL, USER_CATEGORY, USERNAME));

    private static final Set<String> TRG_RESET_PASSWORD_FIELDS = new HashSet<>(Arrays.asList(EMAIL, TENANT_ID));

    private static final Set<String> RESET_PASSWORD_FIELDS =
        new HashSet<>(Arrays.asList(ParameterConstants.PARAM_TOKEN, PASSWORD));

    private static final Set<String> CHANGE_PASSWORD_FIELDS =
        new HashSet<>(Arrays.asList(ParameterConstants.PARAM_OLD_PASSWORD, ParameterConstants.PARAM_NEW_PASSWORD));

    public static final String SELECT_FOR_SIGNIN =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " email = ? OR username = ? AND tenant_id = ?::uuid AND is_deleted = false";

    public static final String SELECT_BY_ID =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_id, tenant_root FROM users WHERE"
            + " id = ?::uuid AND is_deleted = false";

    public static final String SELECT_BY_ID_TENANT_ID =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " id = ?::uuid AND tenant_id = ?::uuid AND is_deleted = false";

    public static final String SELECT_FOR_SIGNUP =
        "SELECT id FROM users WHERE (email = ? OR username = ?) AND tenant_id = ?::uuid";

    public static final String SELECT_BY_REFERENCE_ID_TENANT_ID =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " reference_id = ? AND tenant_id = ?::uuid AND is_deleted = false";

    public static final String SELECT_BY_EMAIL_REFERENCE_ID_TENANT_ID =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " email = ? OR reference_id = ? AND tenant_id = ?::uuid AND is_deleted = false";

    public static final String SELECT_BY_EMAIL =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " email = ? AND is_deleted = false";

    public static final String SELECT_BY_EMAIL_TENANT_ID =
        "SELECT id, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
            + "tenant_root FROM users WHERE"
            + " email = ? AND tenant_id = ?::uuid AND is_deleted = false";

    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(EMAIL, (FieldValidator::validateEmail));
        validatorMap.put(PASSWORD, (FieldValidator::validatePassword));
        validatorMap.put(FIRST_NAME, (FieldValidator::validateFirstName));
        validatorMap.put(LAST_NAME, (FieldValidator::validateLastName));
        validatorMap.put(BIRTH_DATE, (FieldValidator::validateDateofBirth));
        validatorMap.put(TENANT_ID, (FieldValidator::validateUuid));
        validatorMap.put(GENDER, (FieldValidator::validateGenderIfPresent));
        validatorMap.put(USER_CATEGORY, (FieldValidator::validateUserCategoryIfPresent));
        validatorMap.put(ParameterConstants.PARAM_TOKEN, (FieldValidator::validateString));
        validatorMap.put(ParameterConstants.PARAM_OLD_PASSWORD, (FieldValidator::validatePassword));
        validatorMap.put(ParameterConstants.PARAM_NEW_PASSWORD, (FieldValidator::validatePassword));
        return validatorMap;
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(BIRTH_DATE,
            (fieldValue -> FieldConverter.convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
        converterMap.put(PASSWORD, (FieldConverter::convertPasswordToEncryted));
        converterMap.put(USERNAME, (FieldConverter::convertFieldToLowercase));
        converterMap.put(EMAIL, (FieldConverter::convertFieldToLowercase));
        return converterMap;
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new UserValidationRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new UserConverterRegistry();
    }

    public static FieldSelector signupFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(CREATABLE_FIELDS);
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(MANDATORY_FIELDS);
            }
        };
    }

    public static FieldSelector updateFieldSelector() {
        return () -> Collections.unmodifiableSet(UPDATABLE_FIELDS);
    }

    public static FieldSelector triggerResetPasswordEmailFieldSelector() {
        return () -> Collections.unmodifiableSet(TRG_RESET_PASSWORD_FIELDS);
    }

    public static FieldSelector resetPasswordFieldSelector() {
        return () -> Collections.unmodifiableSet(RESET_PASSWORD_FIELDS);
    }

    public static FieldSelector changePasswordFieldSelector() {
        return () -> Collections.unmodifiableSet(CHANGE_PASSWORD_FIELDS);
    }

    private static class UserValidationRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class UserConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }
}
