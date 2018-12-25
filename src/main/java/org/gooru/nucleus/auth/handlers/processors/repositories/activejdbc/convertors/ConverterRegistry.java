package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors;

/**
 * Created by ashish on 28/1/16.
 */
public interface ConverterRegistry {
  FieldConverter lookupConverter(String fieldName);
}
