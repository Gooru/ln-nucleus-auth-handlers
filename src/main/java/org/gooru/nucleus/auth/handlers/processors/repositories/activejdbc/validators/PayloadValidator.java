package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators;

import java.util.Set;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 28/1/16.
 */
public interface PayloadValidator {

  default JsonObject validatePayload(JsonObject input, FieldSelector selector,
      ValidatorRegistry registry) {
    if (input == null || input.isEmpty()) {
      return new JsonObject().put(MessageConstants.MSG_MESSAGE, "null or empty payload");
    }
    
    JsonObject result = new JsonObject();
    input.forEach(entry -> {
      if (selector.allowedFields().contains(entry.getKey())) {
        FieldValidator validator = registry.lookupValidator(entry.getKey());
        if (validator != null) {
          if (!validator.validateField(entry.getValue())) {
            result.put(entry.getKey(), "Invalid value");
          }
        }
      } else {
        result.put(entry.getKey(), "Field not allowed");
      }
    });
    Set<String> mandatory = selector.mandatoryFields();
    if (mandatory != null && mandatory.isEmpty()) {
      mandatory.forEach(s -> {
        if (input.getValue(s) == null) {
          result.put(s, "Missing field");
        }
      });
    }
    return result.isEmpty() ? null : result;
  }
}
