package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

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

class ResetPasswordExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private String emailId;
    private AJEntityUserIdentity userIdentity;
    private JsonObject customizedMailTemplate;
    private String mailTemplateName;

    public ResetPasswordExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        emailId = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_EMAIL_ID);
        ServerValidatorUtility.rejectIfNull(emailId, MessageCodeConstants.AU0014,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode());

        customizedMailTemplate =
            messageContext.requestBody().getJsonObject(ParameterConstants.CUSTOMIZE_MAIL_TEMPLATE);
        if (customizedMailTemplate != null && !customizedMailTemplate.isEmpty()) {
            mailTemplateName = customizedMailTemplate.getString(ParameterConstants.MAIL_TEMPLATE_NAME);
            ServerValidatorUtility.rejectIfNullOrEmpty(mailTemplateName, MessageCodeConstants.AU0052,
                HttpConstants.HttpStatus.BAD_REQUEST.getCode());
        }
    }

    @Override
    public void validateRequest() {
        LazyList<AJEntityUserIdentity> results = AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, emailId);
        userIdentity = results.size() > 0 ? results.get(0) : null;
        ServerValidatorUtility.rejectIfNull(userIdentity, MessageCodeConstants.AU0026,
            HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    }

    @Override
    public MessageResponse executeRequest() {
        final String token = InternalHelper.generatePasswordResetToken(userIdentity.getUserId());
        this.redisClient.set(token, userIdentity.getEmailId(), HelperConstants.EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();

        if (customizedMailTemplate == null || customizedMailTemplate.isEmpty()) {
            mailNotifyBuilder.setTemplateName(MailTemplateConstants.PASSWORD_CHANGE_REQUEST).addToAddress(emailId)
                .putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
                .putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        } else {
            mailNotifyBuilder.setTemplateName(mailTemplateName).addToAddress(emailId)
                .putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
                .putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
            JsonObject mailContext = customizedMailTemplate.getJsonObject(ParameterConstants.MAIL_TEMPLATE_CONTEXT);
            mailContext.forEach(field -> {
                String value = (field.getValue() != null ? field.getValue().toString() : null);
                mailNotifyBuilder.putContext(field.getKey(), value);
            });
        }
        return new MessageResponse.Builder().setResponseBody(null).addMailNotify(mailNotifyBuilder.build())
            .setContentTypeJson().setStatusOkay().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
