package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

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
import org.javalite.activejdbc.LazyList;

class ResetAuthenticateUserPasswordExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private String newPassword;
    private String oldPassword;
    private AJEntityUserIdentity userIdentity;

    public ResetAuthenticateUserPasswordExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
        oldPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_OLD_PASSWORD);
        rejectIfNull(oldPassword, MessageCodeConstants.AU0041, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
        rejectIfNull(newPassword, MessageCodeConstants.AU0042, HttpConstants.HttpStatus.BAD_REQUEST.getCode());

    }

    @Override
    public void validateRequest() {
        LazyList<AJEntityUserIdentity> results =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_ID_PASSWORD, messageContext.user().getUserId(),
                InternalHelper.encryptPassword(oldPassword));
        userIdentity = results.size() > 0 ? results.get(0) : null;
        rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
    }

    @Override
    public MessageResponse executeRequest() {
        userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
        userIdentity.saveIt();
        final String token = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
        this.redisClient.set(token, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
        mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGED)
            .addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
            .putContext(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername())
            .putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        return new MessageResponse.Builder().setResponseBody(null).addMailNotify(mailNotifyBuilder.build())
            .setContentTypeJson().setStatusNoOutput().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
