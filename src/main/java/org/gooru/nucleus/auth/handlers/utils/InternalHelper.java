package org.gooru.nucleus.auth.handlers.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InternalHelper {

    private static final String CLIENT_KEY_HASH = "$GooruCLIENTKeyHash$";

    private static final String COLON = ":";

    public static final String RESET_PASSWORD_TOKEN = "RESET_PASSWORD_TOKEN";

    public static final String EMAIL_CONFIRM_TOKEN = "EMAIL_CONFIRM_TOKEN";

    public final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private static final Logger LOG = LoggerFactory.getLogger(InternalHelper.class);

    public static String generateToken(String clientId, String userId) {
        return Base64.getEncoder().encodeToString(
            (System.currentTimeMillis() + COLON + userId + COLON + clientId).getBytes());
    }

    public static String generateEmailConfirmToken(String userId) {
        return Base64.getEncoder().encodeToString(
            (System.currentTimeMillis() + COLON + userId + COLON + EMAIL_CONFIRM_TOKEN).getBytes());
    }

    public static String generatePasswordResetToken(String userId) {
        return Base64.getEncoder().encodeToString(
            (System.currentTimeMillis() + COLON + userId + COLON + RESET_PASSWORD_TOKEN).getBytes());
    }

    public static String encryptPassword(final String password) {
        return encrypt(password);
    }

    public static String encryptClientKey(final String key) {
        return encrypt(CLIENT_KEY_HASH + key);
    }

    public static String encrypt(final String text) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidRequestException("Error while authenticating user - No algorithm exists");
        }
        messageDigest.update(text.getBytes(StandardCharsets.UTF_8));
        byte raw[] = messageDigest.digest();
        return Base64.getEncoder().encodeToString(raw);
    }

    public static String[] getUsernameAndPassword(String basicAuthCredentials) {
        byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
        final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
        
        int index = credential.indexOf(':');
        if (index <= 0) {
            throw new InvalidRequestException(ServerValidatorUtility.generateErrorMessage(MessageCodeConstants.AU0007));
        }
        
        String[] credentials = new String[2];
        credentials[0] = credential.substring(0, index);
        credentials[1] = credential.substring((index + 1));

        return credentials;
    }

    public static Date isValidDate(String dateAsString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        Date date = null;
        try {
            date = dateFormat.parse(dateAsString.trim());
        } catch (ParseException pe) {
            // FIXME: 1/2/16 AM: Should we do something here
        }
        return date;
    }

    public static int getAge(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTime(date);
        if (dob.after(now)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }
        int year1 = now.get(Calendar.YEAR);
        int year2 = dob.get(Calendar.YEAR);
        int age = year1 - year2;
        int month1 = now.get(Calendar.MONTH);
        int month2 = dob.get(Calendar.MONTH);
        if (month2 > month1) {
            age--;
        } else if (month1 == month2) {
            int day1 = now.get(Calendar.DAY_OF_MONTH);
            int day2 = dob.get(Calendar.DAY_OF_MONTH);
            if (day2 > day1) {
                age--;
            }
        }
        return age;
    }

    public static void executeHTTPClientPost(String url, String data, String authHeader) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            public void run() {
                try {
                    HttpClient httpclient = HttpClientBuilder.create().build();
                    final HttpPost post = new HttpPost(url);
                    post.addHeader(HelperConstants.HEADER_AUTHORIZATION, authHeader);
                    final StringEntity input = new StringEntity(data);
                    post.setEntity(input);
                    httpclient.execute(post);
                } catch (Exception e) {
                    LOG.warn("http post client request was failed!");
                }
            }
        });
    }
    
    public static String encodeToken(String token) {
        try {
            return URLEncoder.encode(token, HelperConstants.CHAR_ENCODING_UTF8);
        } catch (UnsupportedEncodingException uee) {
            return token;
        }
    }

    private InternalHelper() {
        throw new AssertionError();
    }
}
