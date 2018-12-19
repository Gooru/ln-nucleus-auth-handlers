package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors.ConverterRegistry;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors.FieldConverter;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 23-Oct-2017
 */
@Table("domain_based_redirect")
public class AJEntityDomainBasedRedirect extends Model {

  public static final String DOMAIN = "domain";
  public static final String REDIRECT_URL = "redirect_url";
  public static final String TENANT = "tenant";

  public static final String CONTEXT_URL = "context_url";
  public static final String CLASS_CODE = "class_code";

  private static final Set<String> CREATABLE_FIELDS =
      new HashSet<>(Arrays.asList(DOMAIN, CONTEXT_URL));
  private static final Set<String> MANDATORY_FIELDS = new HashSet<>(Arrays.asList(DOMAIN));

  private static final Map<String, FieldValidator> validatorRegistry;
  private static final Map<String, FieldConverter> converterRegistry;

  public static final String FIND_BY_DOMAIN = "domain = ?";
  public static final String FIND_BY_TENANT = "tenant = ?";

  public static final String RESP_STATUS_CODE = "status_code";

  static {
    validatorRegistry = initializeValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(DOMAIN, (FieldValidator::validateString));
    validatorMap.put(CONTEXT_URL, (FieldValidator::validateURLIfPresent));
    return validatorMap;
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    return converterMap;
  }

  public static FieldSelector fieldSelector() {
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

  public static ValidatorRegistry getValidatorRegistry() {
    return new DomainBasedRedirectValidationRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
    return new DomainBasedRedirectConverterRegistry();
  }

  private static class DomainBasedRedirectValidationRegistry implements ValidatorRegistry {
    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return validatorRegistry.get(fieldName);
    }
  }

  private static class DomainBasedRedirectConverterRegistry implements ConverterRegistry {
    @Override
    public FieldConverter lookupConverter(String fieldName) {
      return converterRegistry.get(fieldName);
    }
  }
}
