package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.LazyList;

import io.vertx.core.json.JsonObject;

class UpdateUserEmailExecutor implements DBExecutor {

    private RedisClient redisClient;
    private final MessageContext messageContext;
    private String newEmailId;

    public UpdateUserEmailExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        if (messageContext.requestBody() != null) {
            newEmailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
        }
        rejectIfNull(newEmailId, MessageCodeConstants.AU0014, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            ParameterConstants.PARAM_USER_EMAIL_ID);

    }

    @Override
    public void validateRequest() {
        LazyList<AJEntityUserIdentity> results =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, newEmailId.toLowerCase());
        AJEntityUserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
        reject(userIdentity != null, MessageCodeConstants.AU0023, ParameterConstants.PARAM_USER_EMAIL_ID, HttpConstants.HttpStatus.CONFLICT.getCode(),
            newEmailId, ParameterConstants.EMAIL_ADDRESS);

    }

    @Override
    public MessageResponse executeRequest() {
        LazyList<AJEntityUserIdentity> results =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_USER_ID, messageContext.user().getUserId());
        AJEntityUserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
        ServerValidatorUtility.rejectIfNull(userIdentity, MessageCodeConstants.AU0026,
            HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
        final String token = InternalHelper.generateEmailConfirmToken(messageContext.user().getUserId());
        JsonObject tokenData = new JsonObject();
        tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, newEmailId);
        tokenData.put(ParameterConstants.PARAM_USER_ID, messageContext.user().getUserId());
        this.redisClient.set(token, tokenData.toString(), HelperConstants.EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
        mailNotifyBuilder.setTemplateName(MailTemplateConstants.EMAIL_ADDRESS_CHANGE_REQUEST).addToAddress(newEmailId)
            .putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
            .putContext(ParameterConstants.OLD_EMAIL_ID, userIdentity.getEmailId())
            .putContext(ParameterConstants.NEW_EMAIL_ID, newEmailId);
        return new MessageResponse.Builder().setContentTypeJson().setResponseBody(null)
            .addMailNotify(mailNotifyBuilder.build()).setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
