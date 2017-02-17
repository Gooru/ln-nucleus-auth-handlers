package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.convertors;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.gooru.nucleus.auth.handlers.processors.utils.InternalHelper;
import org.postgresql.util.PGobject;

/**
 * Created by ashish on 28/1/16.
 */
public interface FieldConverter {

    String JSONB_TYPE = "jsonb";
    String UUID_TYPE = "uuid";
    String DATE_TYPE = "date";
    String TEXT_TYPE = "text";

    static PGobject convertFieldToJson(Object value) {
        String JSONB_TYPE = FieldConverter.JSONB_TYPE;
        PGobject pgObject = new PGobject();
        pgObject.setType(JSONB_TYPE);
        try {
            pgObject.setValue(value == null ? null : String.valueOf(value));
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToUuid(String value) {
        String UUID_TYPE = FieldConverter.UUID_TYPE;
        PGobject pgObject = new PGobject();
        pgObject.setType(UUID_TYPE);
        try {
            pgObject.setValue(value);
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToNamedType(Object value, String type) {
        PGobject pgObject = new PGobject();
        pgObject.setType(type);
        try {
            pgObject.setValue(value == null ? null : String.valueOf(value));
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToDateWithFormat(Object o, DateTimeFormatter formatter) {
        if (o == null) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(o.toString(), formatter);
            PGobject date = new PGobject();
            date.setType(DATE_TYPE);
            date.setValue(Date.valueOf(localDate).toString());
            return date;
        } catch (DateTimeParseException | SQLException e) {
            return null;
        }
    }

    static PGobject convertPasswordToEncryted(Object o) {
        /*String CUSTOM_PASSWORD_TYPE = "custom-password";
        PGobject pgObject = new PGobject();
        pgObject.setType(CUSTOM_PASSWORD_TYPE);*/
        PGobject pgObject = new PGobject();
        pgObject.setType(TEXT_TYPE);
        try {
            pgObject.setValue(InternalHelper.encryptPassword(o.toString()));
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToLowercase(Object o) {
        PGobject pgObject = new PGobject();
        pgObject.setType(TEXT_TYPE);
        try {
            pgObject.setValue(o.toString().toLowerCase());
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertEmptyStringToNull(String s) {
        if (s != null && s.isEmpty()) {
            return null;
        }
        PGobject pgObject = new PGobject();
        pgObject.setType(TEXT_TYPE);
        try {
            pgObject.setValue(s);
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    PGobject convertField(Object fieldValue);
}
