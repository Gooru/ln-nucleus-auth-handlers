package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 28/1/16.
 */
public interface FieldValidator {
    static boolean validateStringIfPresent(Object o, int len) {
        return o == null || o instanceof String && !((String) o).isEmpty() && ((String) o).length() < len;
    }

    static boolean validateString(Object o, int len) {
        return !(o == null || !(o instanceof String) || ((String) o).isEmpty() || (((String) o).length() > len));
    }

    static boolean validateString(Object o) {
        return !(o == null || !(o instanceof String) || ((String) o).isEmpty());
    }

    static boolean validateInteger(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    static boolean validateDateWithFormat(Object o, DateTimeFormatter formatter, boolean allowedInPast) {
        if (o == null) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(o.toString(), formatter);
            if (!allowedInPast) {
                return date.isAfter(LocalDate.now());
            }
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    static boolean validateJsonIfPresent(Object o) {
        return o == null || o instanceof JsonObject && !((JsonObject) o).isEmpty();
    }

    static boolean validateJson(Object o) {
        return !(o == null || !(o instanceof JsonObject) || ((JsonObject) o).isEmpty());
    }

    static boolean validateJsonArrayIfPresent(Object o) {
        return o == null || o instanceof JsonArray && !((JsonArray) o).isEmpty();
    }

    static boolean validateJsonArray(Object o) {
        return !(o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty());
    }

    static boolean validateDeepJsonArrayIfPresent(Object o, FieldValidator fv) {
        if (o == null) {
            return true;
        } else if (!(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
            return false;
        } else {
            JsonArray array = (JsonArray) o;
            for (Object element : array) {
                if (!fv.validateField(element)) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean validateDeepJsonArray(Object o, FieldValidator fv) {
        if (o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
            return false;
        }
        JsonArray array = (JsonArray) o;
        for (Object element : array) {
            if (!fv.validateField(element)) {
                return false;
            }
        }
        return true;
    }

    static boolean validateBoolean(Object o) {
        return o != null && o instanceof Boolean;
    }

    static boolean validateBooleanIfPresent(Object o) {
        return o == null || o instanceof Boolean;
    }

    static boolean validateUuid(Object o) {
        try {
            UUID uuid = UUID.fromString((String) o);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean validateUuidIfPresent(String o) {
        return o == null || validateUuid(o);
    }

    static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9]+");
    static boolean validateUsername(Object o) {
        if (o == null) {
            return false;
        }

        String username = o.toString();
        if (username.length() < 4 || username.length() > 20) {
            return false;
        }

        return USERNAME_PATTERN.matcher(username).matches();
    }

    static boolean validateUsernameIfPresent(String o) {
        return o == null || validateUsername(o);
    }

    static boolean validatePassword(Object o) {
        if (o == null) {
            return false;
        }

        String password = o.toString();
        return !(password.length() < 5 || password.length() > 20);
    }

    static final Pattern FIRSTNAME_PATTERN = Pattern.compile("[a-zA-Z0-9'. -]+");
    static boolean validateFirstName(Object o) {
        if (o == null) {
            return false;
        }

        String firstname = o.toString();
        if (firstname.isEmpty() || firstname.length() > 20) {
            return false;
        }

        return FIRSTNAME_PATTERN.matcher(firstname).matches();
    }

    static final Pattern LASTNAME_PATTERN = Pattern.compile("[a-zA-Z0-9'. -]+");
    static boolean validateLastName(Object o) {
        if (o == null) {
            return false;
        }

        String lastname = o.toString();
        if (lastname.isEmpty() || lastname.length() > 20) {
            return false;
        }

        return LASTNAME_PATTERN.matcher(lastname).matches();
    }

    static boolean validateDateofBirth(Object o) {
        if (o == null) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(o.toString().trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    static boolean validateGenderIfPresent(Object o) {
        if (o == null) {
            return true;
        }
        // TODO: Fix this statement as it will always return true. In case object does not match the enum constant, this
        // code will result in IllegalArgumentException and thus making the below code redundant without a catch
        // block for IllegalArgumentException in place
        return HelperConstants.UserGender.valueOf(o.toString()) != null;

    }

    static boolean validateUserCategoryIfPresent(Object o) {
        if (o == null) {
            return true;
        }

        // TODO: Fix this statement as it will always return true. In case object does not match the enum constant, this
        // code will result in IllegalArgumentException and thus making the below code redundant without a catch
        // block for IllegalArgumentException in place
        return HelperConstants.UserCategories.valueOf(o.toString()) != null;

    }

    static boolean validateUserGrantType(Object o) {
        if (o == null) {
            return false;
        }

        // TODO: Fix this statement as it will always return true. In case object does not match the enum constant, this
        // code will result in IllegalArgumentException and thus making the below code redundant without a catch
        // block for IllegalArgumentException in place
        return HelperConstants.GrantTypes.valueOf(o.toString()) != null;
    }

    boolean validateField(Object value);

    Pattern EMAIL_PATTERN =
        Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    static boolean validateEmail(Object o) {
        Matcher matcher = EMAIL_PATTERN.matcher((String) o);
        return matcher.matches();
    }
}
