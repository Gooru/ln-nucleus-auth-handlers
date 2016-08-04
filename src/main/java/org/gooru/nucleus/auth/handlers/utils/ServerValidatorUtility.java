package org.gooru.nucleus.auth.handlers.utils;

import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.CONFLICT;
import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.FORBIDDEN;
import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.NOT_FOUND;
import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.UNAUTHORIZED;
import static org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus.GONE;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.processors.exceptions.AccessDeniedException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.ConflictException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.GoneException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.NotFoundException;
import org.gooru.nucleus.auth.handlers.processors.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerValidatorUtility {

    private static final ResourceBundle MESSAGE = ResourceBundle.getBundle("message");
    private static Logger LOGGER = LoggerFactory.getLogger(ServerValidatorUtility.class);

    public static void addValidatorIfNullError(final JsonObject errors, final String fieldName, final Object data,
        final String code, final String... placeHolderReplacer) {
        if (data == null) {
            addError(errors, fieldName, code, placeHolderReplacer);
        }
    }

    public static void addValidatorIfNullOrEmptyError(final JsonObject errors, final String fieldName,
        final String data, final String code, final String... placeHolderReplacer) {
        if (data == null || data.trim().length() == 0) {
            addError(errors, fieldName, code, placeHolderReplacer);
        }
    }

    public static void addValidator(final JsonObject errors, final Boolean data, final String fieldName,
        final String code, final String... placeHolderReplacer) {
        if (data) {
            addError(errors, fieldName, code, placeHolderReplacer);
        }
    }

    public static void rejectIfNull(final Object data, final String code, final int errorCode,
        final String... placeHolderReplacer) {
        if (data == null) {
            exceptionHandler(errorCode, code, placeHolderReplacer);
        }
    }

    public static void rejectIfNullOrEmpty(final String data, final String code, final int errorCode,
        final String... placeHolderReplacer) {
        if (data == null || data.trim().length() == 0) {
            exceptionHandler(errorCode, code, placeHolderReplacer);
        }
    }

    public static void reject(final Boolean data, final String code, final int errorCode,
        final String... placeHolderReplacer) {
        if (data) {
            exceptionHandler(errorCode, code, placeHolderReplacer);
        }
    }
    
    public static void reject(final Boolean data, final String code, final String fieldName, final int errorCode,
        final String... placeHolderReplacer) {
        if (data) {
            JsonObject errors = new JsonObject();
            errors.put(fieldName, generateErrorMessage(code, placeHolderReplacer));
            rejectError(errors, errorCode);
        }
    }
    
    private static void exceptionHandler(final int errorCode, final String code, final String... placeHolderReplacer) {
        if (errorCode == NOT_FOUND.getCode()) {
            throw new NotFoundException(generateErrorMessage(code, placeHolderReplacer));
        } else if (errorCode == FORBIDDEN.getCode()) {
            throw new AccessDeniedException(generateErrorMessage(code, placeHolderReplacer));
        } else if (errorCode == UNAUTHORIZED.getCode()) {
            throw new UnauthorizedException(generateErrorMessage(code, placeHolderReplacer));
        } else if (errorCode == BAD_REQUEST.getCode()) {
            throw new BadRequestException(generateErrorMessage(code, placeHolderReplacer));
        } else if (errorCode == CONFLICT.getCode()) {
            throw new ConflictException(generateErrorMessage(code, placeHolderReplacer));
        } else if (errorCode == GONE.getCode()) {
            throw new GoneException(generateErrorMessage(code, placeHolderReplacer));
        }
    }

    public static String generateErrorMessage(final String errorCode) {
        return MESSAGE.getString(errorCode);
    }

    public static String generateErrorMessage(final String errorCode, final String... params) {
        String errorMsg = MESSAGE.getString(errorCode);
        if (params != null) {
            for (int index = 0; index < params.length; index++) {
                errorMsg = errorMsg.replace("{" + index + "}", params[index]);
            }
        }
        return errorMsg;
    }

    public static String generateMessage(final String code, final String... params) {
        String msg = MESSAGE.getString(code);
        if (params != null) {
            for (int index = 0; index < params.length; index++) {
                msg = msg.replace("{" + index + "}", params[index] == null ? "" : params[index]);
            }
        }
        return msg;
    }

    public static String generateMessage(String rawData, final Map<String, Object> data) {
        if (rawData != null && data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                rawData =
                    rawData.replace("[" + entry.getKey() + "]",
                        entry.getValue() == null ? "" : (String) entry.getValue());
            }
        }
        return rawData;
    }

    public static void rejectError(final JsonObject errors, final int errorCode) {
        if (errors != null && !errors.isEmpty()) {
            if (errorCode == BAD_REQUEST.getCode()) {
                throw new BadRequestException(errors.toString());
            } else if(errorCode == CONFLICT.getCode()) { 
                throw new ConflictException(errors.toString());
            } else if(errorCode == GONE.getCode()) { 
                throw new GoneException(errors.toString());
            }
        }
    }

    public static void throwASInternalServerError() {
        throw new RuntimeException("internal api error");
    }

    public static void reject(Throwable e) {
        if (e instanceof BadRequestException) {
            throw new BadRequestException(e);
        } else if (e instanceof NotFoundException) {
            throw new NotFoundException(e);
        } else if (e instanceof AccessDeniedException) {
            throw new AccessDeniedException(e);
        } else if (e instanceof UnauthorizedException) {
            throw new UnauthorizedException(e);
        } else if (e instanceof ConflictException) { 
            throw new ConflictException(e);
        } else if (e instanceof GoneException) { 
            throw new GoneException(e);
        } else {
            LOGGER.error("something went wrong", e);
            throw new RuntimeException("internal api error");
        }
    }

    public static void addError(JsonObject errors, String fieldName, String code, String... placeHolderReplacer) {
        errors.put(fieldName, generateErrorMessage(code, placeHolderReplacer));
    }

}
