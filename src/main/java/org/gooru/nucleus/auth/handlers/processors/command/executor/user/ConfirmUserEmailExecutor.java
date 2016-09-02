package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.javalite.activejdbc.LazyList;

class ConfirmUserEmailExecutor implements DBExecutor {

    private RedisClient redisClient;
    private MessageContext messageContext;
    private JsonObject accessToken;
    private AJEntityUserIdentity userIdentity;
    private String token;

    public ConfirmUserEmailExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        if (messageContext.requestBody() != null) {
            token = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_TOKEN);
        }
        rejectIfNull(token, MessageCodeConstants.AU0030, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        final String tokenData = this.redisClient.get(token);
        rejectIfNull(tokenData, MessageCodeConstants.AU0030, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        this.accessToken = new JsonObject(tokenData);
    }

    @Override
    public void validateRequest() {
        final String userId = accessToken.getString(ParameterConstants.PARAM_USER_ID);
        LazyList<AJEntityUserIdentity> results =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_USER_ID, userId);
        userIdentity = (results.size() > 0) ? results.get(0) : null;
        rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
    }

    @Override
    public MessageResponse executeRequest() {

        return confirmUserEmail();
    }

    private MessageResponse confirmUserEmail() {
        final String emailId = accessToken.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setEventName(Event.UPDATE_USER_EMAIL_CONFIRM.getName());

        if (!userIdentity.getEmailId().equalsIgnoreCase(emailId)) {
            userIdentity.setEmailId(emailId);
            LazyList<AJEntityUser> results = AJEntityUser.where(AJEntityUser.GET_USER, userIdentity.getUserId());
            AJEntityUser user = (results.size() > 0) ? results.get(0) : null;
            user.setEmailId(emailId.toLowerCase());
            user.saveIt();

            eventBuilder.put(SchemaConstants.USER_DEMOGRAPHIC,
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, HelperConstants.USERS_JSON_FIELDS).toJson(user));
        }
        MailNotifyBuilder mailNotifyBuilder = null;
        if (!userIdentity.getEmailConfirmStatus()) {
            // build the mail notify for welcome email.
            mailNotifyBuilder = new MailNotifyBuilder();
            mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).addToAddress(
                userIdentity.getEmailId());
        }
        userIdentity.setEmailConfirmStatus(true);
        userIdentity.saveIt();
        this.redisClient.del(token);

        eventBuilder.put(SchemaConstants.USER_IDENTITY, JsonFormatterBuilder.buildSimpleJsonFormatter(false, null)
            .toJson(userIdentity));
        return new MessageResponse.Builder().setEventData(eventBuilder.build())
            .addMailNotify(mailNotifyBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
}
