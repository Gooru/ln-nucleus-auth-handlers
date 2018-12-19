package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators;

import java.util.*;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

/**
 * @author szgooru Created On: 05-Jan-2017
 */
public final class RequestValidator {

  private static final String CLIENT_ID = ParameterConstants.PARAM_CLIENT_ID;
  private static final String CLIENT_KEY = ParameterConstants.PARAM_CLIENT_KEY;
  private static final String ANONYMOUS_TOKEN = ParameterConstants.PARAM_ANONYMOUS_TOKEN;
  private static final String USER_CATEGORY = "user_category";
  private static final String GRANT_TYPE = ParameterConstants.PARAM_GRANT_TYPE;
  private static final String USER = ParameterConstants.PARAM_USER;
  private static final String APP_ID = ParameterConstants.PARAM_APP_ID;

  private static final Map<String, FieldValidator> validatorRegistry;

  private static final Set<String> LTISSO_FIELDS = new HashSet<>(Arrays.asList(GRANT_TYPE, USER));
  private static final Set<String> WSFEDSSO_FIELDS = new HashSet<>(Arrays.asList(GRANT_TYPE, USER));

  private static final Set<String> AUTHORIZE_ALLOWED_FIELDS = new HashSet<>(
      Arrays.asList(CLIENT_ID, CLIENT_KEY, ANONYMOUS_TOKEN, GRANT_TYPE, USER, APP_ID));

  private static final Set<String> AUTHORIZE_MANDATORY_FIELDS =
      new HashSet<>(Arrays.asList(GRANT_TYPE));

  static {
    validatorRegistry = initializeValidators();
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(USER_CATEGORY, (FieldValidator::validateUserCategoryIfPresent));
    validatorMap.put(CLIENT_ID, (FieldValidator::validateUuid));
    validatorMap.put(CLIENT_KEY, (FieldValidator::validateString));
    validatorMap.put(GRANT_TYPE, (FieldValidator::validateUserGrantType));
    validatorMap.put(USER, (FieldValidator::validateJson));
    validatorMap.put(APP_ID, (FieldValidator::validateUuid));
    return validatorMap;
  }

  public static FieldSelector ltissoFieldSelector() {
    return () -> Collections.unmodifiableSet(LTISSO_FIELDS);
  }

  public static FieldSelector wsfedssoFieldSelector() {
    return () -> Collections.unmodifiableSet(WSFEDSSO_FIELDS);
  }

  public static FieldSelector authorizeFieldSelector() {
    return new FieldSelector() {

      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(AUTHORIZE_ALLOWED_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(AUTHORIZE_MANDATORY_FIELDS);
      }
    };
    // return () -> Collections.unmodifiableSet(AUTHORIZE_FIELDS);
  }

  public static ValidatorRegistry getValidatorRegistry() {
    return new RequestValidationRegistry();
  }

  private static class RequestValidationRegistry implements ValidatorRegistry {
    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return validatorRegistry.get(fieldName);
    }
  }

  private RequestValidator() {
    throw new AssertionError();
  }
}
