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

class ResetUnAuthenticateUserPasswordExecutor implements DBExecutor {

    private RedisClient redisClient;
    private final MessageContext messageContext;
    private String newPassword;
    private String token;
    private AJEntityUserIdentity userIdentity;

    public ResetUnAuthenticateUserPasswordExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
        newPassword = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_NEW_PASSWORD);
        rejectIfNull(token, MessageCodeConstants.AU0046, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            ParameterConstants.PARAM_USER_TOKEN);
        rejectIfNull(newPassword, MessageCodeConstants.AU0046, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            ParameterConstants.PARAM_USER_NEW_PASSWORD);
    }

    @Override
    public void validateRequest() {
        String emailId = this.redisClient.get(token);        
        reject(emailId == null, MessageCodeConstants.AU0028, ParameterConstants.PARAM_USER_TOKEN, HttpConstants.HttpStatus.GONE.getCode());
        LazyList<AJEntityUserIdentity> results = AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, emailId.toLowerCase());
        userIdentity = results.size() > 0 ? results.get(0) : null;
        ServerValidatorUtility.rejectIfNull(userIdentity, MessageCodeConstants.AU0026,
            HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    }

    @Override
    public MessageResponse executeRequest() {
        userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
        userIdentity.saveIt();
        this.redisClient.del(token);
        final String newtoken = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
        this.redisClient.set(newtoken, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
        mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGED)
            .addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(newtoken))
            .putContext(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername())
            .putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        return new MessageResponse.Builder().addMailNotify(mailNotifyBuilder.build()).setContentTypeJson()
            .setStatusNoOutput().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
