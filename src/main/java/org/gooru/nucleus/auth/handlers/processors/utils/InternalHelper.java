package org.gooru.nucleus.auth.handlers.processors.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.ResourceBundle;

import org.gooru.nucleus.auth.handlers.app.components.AppHttpClient;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClientRequest;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public final class InternalHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalHelper.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(HelperConstants.RESOURCE_BUNDLE);

    private static final String TOKEN_VERSION = "2";
    private static final String RESET_PASSWORD_TOKEN = "RESET_PASSWORD_TOKEN";
    private static final String CLIENT_KEY_HASH = "$GooruCLIENTKeyHash$";
    private static final String COLON = ":";

    private InternalHelper() {
        throw new AssertionError();
    }

    public static String encryptClientKey(final String key) {
        return encrypt(CLIENT_KEY_HASH + key);
    }

    private static String encrypt(final String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(text.getBytes(StandardCharsets.UTF_8));
            byte raw[] = messageDigest.digest();
            return Base64.getEncoder().encodeToString(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidRequestException("Error while authenticating user - No algorithm exists");
        }
    }

    public static String generateToken(String userId, String partnerId, String clientId) {
        String result = TOKEN_VERSION + COLON + System.currentTimeMillis() + COLON + userId + COLON
            + (partnerId != null ? partnerId : "") + COLON + clientId;

        return Base64.getEncoder().encodeToString(result.getBytes());
    }

    public static String[] getUsernameAndPassword(String basicAuthCredentials) {
        byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
        final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);

        int index = credential.indexOf(':');
        if (index <= 0) {
            throw new InvalidRequestException(RESOURCE_BUNDLE.getString("invalid.credential"));
        }

        String[] credentials = new String[2];
        credentials[0] = credential.substring(0, index);
        credentials[1] = credential.substring((index + 1));

        return credentials;
    }

    public static String[] getClientIdAndSecret(String basicAuthCredentials) {
        byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
        final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);

        int index = credential.indexOf(':');
        if (index <= 0) {
            throw new InvalidRequestException(RESOURCE_BUNDLE.getString("invalid.credential"));
        }

        String[] credentials = new String[2];
        credentials[0] = credential.substring(0, index);
        credentials[1] = credential.substring((index + 1));

        return credentials;
    }

    public static String encryptPassword(final String password) {
        return encrypt(password);
    }

    public static String generatePasswordResetToken(String userId) {
        return Base64.getEncoder()
            .encodeToString((System.currentTimeMillis() + COLON + userId + COLON + RESET_PASSWORD_TOKEN).getBytes());
    }

    public static String encodeToken(String token) {
        try {
            return URLEncoder.encode(token, HelperConstants.CHAR_ENCODING_UTF8);
        } catch (UnsupportedEncodingException uee) {
            return token;
        }
    }

    public static void executeHTTPClientPost(String data, String authHeader) {
        try {
            AppHttpClient httpClient = AppHttpClient.getInstance();
            HttpClientRequest eventRequest = httpClient.getHttpClient().post(httpClient.endpoint(), responseHandler -> {
                if (responseHandler.statusCode() == HttpConstants.HttpStatus.SUCCESS.getCode()) {
                    LOGGER.info("event posted successfully");
                } else {
                    LOGGER.warn("event post failed with status code: {}, event data: {}", responseHandler.statusCode(),
                        data);
                }
            });
            
            eventRequest.putHeader(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);
            eventRequest.putHeader(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(data.getBytes().length));
            eventRequest.putHeader(HttpConstants.HEADER_AUTH, authHeader);
            eventRequest.write(data);
            eventRequest.end();
        } catch (Throwable t) {
            LOGGER.error("error while posting event", t);
        }
        LOGGER.info("request end");
    }
}
