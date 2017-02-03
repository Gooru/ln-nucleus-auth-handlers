package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

/**
 * @author szgooru Created On: 05-Jan-2017
 */
public final class RequestValidator {

    private static final String CLIENT_ID = ParameterConstants.PARAM_CLIENT_ID;
    private static final String CLIENT_KEY = ParameterConstants.PARAM_CLIENT_KEY;
    private static final String USER_CATEGORY = "user_category";
    private static final String GRANT_TYPE = ParameterConstants.PARAM_GRANT_TYPE;
    private static final String USER = ParameterConstants.PARAM_USER;
    
    private static final Map<String, FieldValidator> validatorRegistry;

    public static final Set<String> LTISSO_FIELDS =
        new HashSet<>(Arrays.asList(CLIENT_ID, CLIENT_KEY, USER_CATEGORY));

    public static final Set<String> AUTHORIZE_FIELDS =
        new HashSet<>(Arrays.asList(CLIENT_ID, CLIENT_KEY, GRANT_TYPE, USER));
    
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
        return validatorMap;
    }

    public static FieldSelector ltissoFieldSelector() {
        return () -> Collections.unmodifiableSet(LTISSO_FIELDS);
    }

    public static FieldSelector authorizeFieldSelector() {
        return () -> Collections.unmodifiableSet(AUTHORIZE_FIELDS);
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
