package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.LazyList;

public final class FetchAccessTokenExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private String token;

    public FetchAccessTokenExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);

    }

    @Override
    public void validateRequest() {

    }

    @Override
    public MessageResponse executeRequest() {
        JsonObject accessToken = this.redisClient.getJsonObject(token);
        ServerValidatorUtility.reject(accessToken == null, MessageCodeConstants.AU0040, 400);
        if (accessToken.containsKey(MessageConstants.MSG_KEY_PREFS)) {
            accessToken.remove(MessageConstants.MSG_KEY_PREFS);
        }
        accessToken.remove(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
        final String userId = accessToken.getString(ParameterConstants.PARAM_USER_ID);
        if (!userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userId);
            AJEntityUser user = users.size() > 0 ? users.get(0) : null;
            if (user != null) {
                if (user.getFirstname() != null) {
                    accessToken.put(ParameterConstants.PARAM_USER_FIRSTNAME, user.getFirstname());
                }
                if (user.getLastname() != null) {
                    accessToken.put(ParameterConstants.PARAM_USER_LASTNAME, user.getLastname());
                }
                if (user.getUserCategory() != null) {
                    accessToken.put(ParameterConstants.PARAM_USER_CATEGORY, user.getUserCategory());
                }
                if (user.getThumbnailPath() != null) {
                    accessToken.put(ParameterConstants.PARAM_USER_THUMBNAIL_PATH, user.getThumbnailPath());
                }
            }
        }
        return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay()
            .successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
}
