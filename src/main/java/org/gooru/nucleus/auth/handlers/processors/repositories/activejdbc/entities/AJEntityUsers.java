package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
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
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 03-Jan-2017
 */

@Table("users")
public class AJEntityUsers extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityUsers.class);
  public static final String TABLE = "users";

  public static final String ID = "id";
  public static final String USERNAME = "username";
  public static final String DISPLAY_NAME = "display_name";
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
  public static final String PARTNER_ID = "partner_id";
  public static final String IS_DELETED = "is_deleted";

  private static final String APP_ID = "app_id";

  private static final String UUID_TYPE = "uuid";

  private static final Set<String> CREATABLE_FIELDS = new HashSet<>(Arrays.asList(USERNAME, EMAIL,
      PASSWORD, BIRTH_DATE, FIRST_NAME, LAST_NAME, TENANT_ID, GENDER, USER_CATEGORY, APP_ID));

  private static final Set<String> MANDATORY_FIELDS = new HashSet<>(
      Arrays.asList(USERNAME, EMAIL, PASSWORD, BIRTH_DATE, FIRST_NAME, LAST_NAME, TENANT_ID));

  private static final Set<String> UPDATABLE_FIELDS = new HashSet<>(Arrays.asList(ABOUT, FIRST_NAME,
      LAST_NAME, COUNTRY, COUNTRY_ID, GRADE, ROSTER_GLOBAL_USERID, SCHOOL_DISTRICT_ID,
      SCHOOL_DISTRICT, STATE, STATE_ID, THUMBNAIL, USER_CATEGORY, USERNAME));

  private static final Set<String> TRG_RESET_PASSWORD_FIELDS =
      new HashSet<>(Arrays.asList(EMAIL, TENANT_ID));

  private static final Set<String> RESET_PASSWORD_FIELDS =
      new HashSet<>(Arrays.asList(ParameterConstants.PARAM_TOKEN, PASSWORD));

  private static final Set<String> CHANGE_PASSWORD_FIELDS =
      new HashSet<>(Arrays.asList(ParameterConstants.PARAM_OLD_PASSWORD,
          ParameterConstants.PARAM_NEW_PASSWORD, ParameterConstants.PARAM_SEND_EMAIL));

  public static final String SELECT_FOR_SIGNIN =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE (email = ? OR username = ?) AND tenant_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_id, tenant_root FROM users WHERE id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_ID_TENANT_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE id = ?::uuid AND tenant_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_ID_PARTNER_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE id = ?::uuid AND partner_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_FOR_SIGNUP =
      "SELECT id, username, email FROM users WHERE (email = ? OR username = ?) AND tenant_id = ?::uuid";

  public static final String SELECT_BY_REFERENCE_ID_TENANT_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE reference_id = ? AND tenant_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_REFERENCE_ID_PARTNER_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE reference_id = ? AND partner_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_EMAIL =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE email = ? AND is_deleted = false";

  public static final String SELECT_BY_EMAIL_TENANT_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE email = ? AND tenant_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_EMAIL_PARTNER_ID =
      "SELECT id, display_name, username, email, first_name, last_name, password, login_type, user_category, thumbnail, "
          + "tenant_root FROM users WHERE email = ? AND partner_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_BY_USERNAME_TENANT_ID = "username = ? AND tenant_id = ?::uuid";
  public static final String SELECT_BY_USERNAME_PARTNER_ID =
      "username = ? AND partner_id = ?::uuid";

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
    validatorMap.put(USERNAME, (FieldValidator::validateUsername));
    validatorMap.put(BIRTH_DATE, (FieldValidator::validateDateofBirth));
    validatorMap.put(TENANT_ID, (FieldValidator::validateUuid));
    validatorMap.put(GENDER, (FieldValidator::validateGenderIfPresent));
    validatorMap.put(USER_CATEGORY, (FieldValidator::validateUserCategoryIfPresent));
    validatorMap.put(ParameterConstants.PARAM_TOKEN, (FieldValidator::validateString));
    validatorMap.put(ParameterConstants.PARAM_OLD_PASSWORD, (FieldValidator::validatePassword));
    validatorMap.put(ParameterConstants.PARAM_NEW_PASSWORD, (FieldValidator::validatePassword));
    validatorMap.put(COUNTRY_ID, (FieldValidator::validateUuidIfPresent));
    validatorMap.put(STATE_ID, (FieldValidator::validateUuidIfPresent));
    validatorMap.put(SCHOOL_DISTRICT_ID, (FieldValidator::validateUuidIfPresent));
    validatorMap.put(STATE,
        (fieldValue -> FieldValidator.validateStringAllowNullOrEmpty(fieldValue, 2000)));
    validatorMap.put(COUNTRY,
        (fieldValue -> FieldValidator.validateStringAllowNullOrEmpty(fieldValue, 2000)));
    validatorMap.put(ABOUT,
        (fieldValue -> FieldValidator.validateStringAllowNullOrEmpty(fieldValue, 5000)));
    validatorMap.put(THUMBNAIL,
        (fieldValue -> FieldValidator.validateStringAllowNullOrEmpty(fieldValue, 1000)));
    validatorMap.put(ROSTER_GLOBAL_USERID,
        (fieldValue -> FieldValidator.validateStringAllowNullOrEmpty(fieldValue, 512)));
    return validatorMap;
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(TENANT_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(PARTNER_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(BIRTH_DATE, (fieldValue -> FieldConverter
        .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    converterMap.put(PASSWORD,
        (fieldValue -> FieldConverter.convertPasswordToEncryted(fieldValue)));
    converterMap.put(USERNAME, (fieldValue -> FieldConverter.convertFieldToLowercase(fieldValue)));
    converterMap.put(EMAIL, (fieldValue -> FieldConverter.convertFieldToLowercase(fieldValue)));
    converterMap.put(REFERENCE_ID,
        (fieldValue -> FieldConverter.convertFieldToLowercase(fieldValue)));
    converterMap.put(GRADE, (fieldValue -> FieldConverter.convertFieldToJson(fieldValue)));
    converterMap.put(COUNTRY_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(STATE_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(SCHOOL_DISTRICT_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(THUMBNAIL,
        (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
    converterMap.put(ABOUT,
        (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
    converterMap.put(STATE,
        (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
    converterMap.put(COUNTRY,
        (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
    converterMap.put(ROSTER_GLOBAL_USERID,
        (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));

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

  public void setTenantId(String value) {
    setPGObject(TENANT_ID, UUID_TYPE, value);
  }

  public void setTenantRoot(String tenantRoot) {
    setPGObject(TENANT_ROOT, UUID_TYPE, tenantRoot);
  }

  public void setPartnerId(String value) {
    setPGObject(PARTNER_ID, UUID_TYPE, value);
  }

  private void setPGObject(String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      this.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      this.errors().put(field, value);
    }
  }
}
