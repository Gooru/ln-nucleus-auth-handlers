package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;

import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

import io.vertx.core.json.JsonObject;

class ResendConfirmationEmailExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private AJEntityUser user;
    private AJEntityUserIdentity userIdentity;
    private static final int EXPIRE_IN_SECONDS = 86400;

    public ResendConfirmationEmailExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {

    }

    @Override
    public void validateRequest() {
        LazyList<AJEntityUser> results = AJEntityUser.where(AJEntityUser.GET_USER, messageContext.user().getUserId());
        System.out.println(messageContext.user().getUserId());
        user = results.size() > 0 ? results.get(0) : null;
        rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
        LazyList<AJEntityUserIdentity> userIdentitys =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, user.getEmailId().toLowerCase());
        userIdentity = userIdentitys.size() > 0 ? userIdentitys.get(0) : null;
        rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
        reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED),
            MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
    }

    @Override
    public MessageResponse executeRequest() {
        final String token = InternalHelper.generateEmailConfirmToken(messageContext.user().getUserId());
        JsonObject tokenData = new JsonObject();
        tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        tokenData.put(ParameterConstants.PARAM_USER_ID, user.getId().toString());
        this.redisClient.set(token, tokenData.toString(), EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailConfirmNotifyBuilder = new MailNotifyBuilder();
        mailConfirmNotifyBuilder.setTemplateName(MailTemplateConstants.USER_REGISTARTION_CONFIRMATION)
            .addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
            .putContext(ParameterConstants.PARAM_USER_ID, user.getId().toString());
        return new MessageResponse.Builder().addMailNotify(mailConfirmNotifyBuilder.build()).setContentTypeJson()
            .setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
