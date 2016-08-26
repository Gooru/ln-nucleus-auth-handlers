package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidatorIfNullOrEmptyError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonObject;

import java.util.Date;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.UserContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

class CreateUserExecutor implements DBExecutor {

    private RedisClient redisClient;
    private final MessageContext messageContext;
    private UserDTO userDTO;
    private Date dob;
    private AJEntityUser user;

    public CreateUserExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        userDTO = new UserDTO(messageContext.requestBody());
        rejectIfNullOrEmpty(userDTO.getBirthDate(), MessageCodeConstants.AU0015, 400,
            ParameterConstants.PARAM_USER_BIRTH_DATE);
        dob = InternalHelper.isValidDate(userDTO.getBirthDate());
        rejectIfNull(dob, MessageCodeConstants.AU0022, 400, ParameterConstants.PARAM_USER_BIRTH_DATE);
    }

    @Override
    public void validateRequest() {
        ActionResponseDTO<AJEntityUser> userValidator = createUserValidator(userDTO);
        userValidator.getModel().setBirthDate(dob);
        user = userValidator.getModel();
    }

    @Override
    public MessageResponse executeRequest() {
        user.saveIt();
        UserContext userContext = messageContext.user();
        AJEntityUserIdentity userIdentity = createUserIdentityValue(userDTO, user, userContext.getClientId());
        userIdentity.saveIt();
        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, userContext.getClientId());
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, userContext.getCdnUrls());
        JsonObject prefs = new JsonObject();
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        final String token = InternalHelper.generateToken(userContext.getClientId(), userIdentity.getUserId());
        saveAccessToken(token, accessToken, userContext.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);

        final EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, HelperConstants.USERS_JSON_FIELDS).toJson(user));
        eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).toJson(userIdentity));
        eventBuilder.setEventName(Event.CREATE_USER.getName());
        // generate email confirmation token and build the mail notify.
        final String emailToken = InternalHelper.generateEmailConfirmToken(userIdentity.getUserId());
        JsonObject tokenData = new JsonObject();
        tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        tokenData.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        this.redisClient.set(emailToken, tokenData.toString(), HelperConstants.EXPIRE_IN_SECONDS);
        MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
        final ConfigRegistry configRegistry = ConfigRegistry.instance();
        if (configRegistry.sendConfirmationEmail()) {
            mailNotifyBuilder.setTemplateName(MailTemplateConstants.USER_REGISTARTION_CONFIRMATION)
                .addToAddress(userIdentity.getEmailId())
                .putContext(ParameterConstants.MAIL_TOKEN, InternalHelper.encodeToken(token))
                .putContext(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        } else {
            mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).addToAddress(
                userIdentity.getEmailId());
        }
        return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .addMailNotify(mailNotifyBuilder.build())
            .setHeader(HelperConstants.LOCATION, HelperConstants.USER_ENTITY_URI + userIdentity.getUserId())
            .setContentTypeJson().setStatusCreated().successful().build();
    }

    private ActionResponseDTO<AJEntityUser> createUserValidator(final UserDTO userDTO) {
        final JsonObject errors = new JsonObject();
        final AJEntityUser user = new AJEntityUser();
        final String firstname = userDTO.getFirstname();
        if (firstname != null) {
            addValidator(errors, !(firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME,
                MessageCodeConstants.AU0021, ParameterConstants.PARAM_USER_FIRSTNAME);
            user.setFirstname(firstname);
        }
        final String lastname = userDTO.getLastname();
        if (lastname != null) {
            addValidator(errors, !(lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME,
                MessageCodeConstants.AU0021, ParameterConstants.PARAM_USER_LASTNAME);
            user.setLastname(lastname);
        }
        final String username = userDTO.getUsername();
        addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_USERNAME, username,
            MessageCodeConstants.AU0013);
        if (username != null) {
            addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME,
                MessageCodeConstants.AU0017);
            addValidator(errors, ((username.length() < 4 || username.length() > 20)),
                ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
                ParameterConstants.PARAM_USER_USERNAME, "4", "20");
        }
        final String emailId = userDTO.getEmailId();
        addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_EMAIL_ID, userDTO.getEmailId(),
            MessageCodeConstants.AU0014);
        if (emailId != null) {
            addValidator(errors, !(emailId.indexOf("@") > 1), ParameterConstants.PARAM_USER_EMAIL_ID,
                MessageCodeConstants.AU0020);
        }
        final String userCategory = userDTO.getUserCategory();
        if (userCategory != null) {
            addValidator(errors, (HelperConstants.USER_CATEGORY.get(userCategory) == null),
                ParameterConstants.PARAM_USER_CATEGORY, MessageCodeConstants.AU0025);
            user.setUserCategory(userCategory);

        }
        final String password = userDTO.getPassword();
        addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password,
            MessageCodeConstants.AU0016);
        if (password != null) {
            addValidator(errors, ((password.length() < 5 || password.length() > 14)),
                ParameterConstants.PARAM_USER_PASSWORD, MessageCodeConstants.AU0018,
                ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

        }

        user.setEmailId(emailId.toLowerCase());
        if (userDTO.getGrade() != null) {
            user.setGrade(userDTO.getGrade());
        }
        if (user.getGender() != null) {
            addValidator(errors, (HelperConstants.USER_GENDER.get(user.getGender()) == null),
                ParameterConstants.PARAM_USER_GENDER, MessageCodeConstants.AU0024);
            user.setGender(user.getGender());
        }
        if (userDTO.getCountryId() != null) {
            LazyList<AJEntityCountry> results =
                AJEntityCountry.where(AJEntityCountry.GET_COUNTRY_BY_ID, userDTO.getCountryId());
            AJEntityCountry country = results.size() > 0 ? results.get(0) : null;
            addValidator(errors, (country == null), ParameterConstants.PARAM_USER_COUNTRY_ID,
                MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_COUNTRY);
            if (country != null) {
                user.setCountryId(country.getId());
                user.setCountry(country.getName());
            }
        } else if (userDTO.getCountry() != null) {
            user.setCountry(userDTO.getCountry());
        }

        if (userDTO.getStateId() != null) {
            LazyList<AJEntityState> results = AJEntityState.where(AJEntityState.GET_STATE_BY_ID, userDTO.getStateId());
            AJEntityState state = results.size() > 0 ? results.get(0) : null;
            addValidator(errors, (state == null), ParameterConstants.PARAM_USER_STATE_ID, MessageCodeConstants.AU0027,
                ParameterConstants.PARAM_USER_STATE);
            if (state != null) {
                user.setStateId(state.getId());
                user.setState(state.getName());
            }
        } else if (userDTO.getState() != null) {
            user.setState(userDTO.getState());
        }

        if (userDTO.getSchoolDistrictId() != null) {
            LazyList<AJEntitySchoolDistrict> results =
                AJEntitySchoolDistrict.where(AJEntitySchoolDistrict.GET_SCHOOL_DISTRICT_BY_ID,
                    userDTO.getSchoolDistrictId());
            AJEntitySchoolDistrict schoolDistrict = results.size() > 0 ? results.get(0) : null;
            addValidator(errors, (schoolDistrict == null), ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID,
                MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
            if (schoolDistrict != null) {
                user.setSchoolDistrictId(schoolDistrict.getId());
                user.setSchoolDistrict(schoolDistrict.getName());
            }
        } else if (userDTO.getSchoolDistrict() != null) {
            user.setSchoolDistrict(userDTO.getSchoolDistrict());
        }

        if (userDTO.getSchoolId() != null) {
            LazyList<AJEntitySchool> results =
                AJEntitySchool.where(AJEntitySchool.GET_SCHOOL_BY_ID, userDTO.getSchoolId());
            AJEntitySchool school = results.size() > 0 ? results.get(0) : null;
            addValidator(errors, (school == null), ParameterConstants.PARAM_USER_SCHOOL_ID,
                MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_SCHOOL);
            if (school != null) {
                user.setSchoolId(school.getId());
                user.setSchool(school.getName());
            }
        } else if (userDTO.getSchool() != null) {
            user.setSchool(userDTO.getSchool());
        }
        rejectError(errors, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
        LazyList<AJEntityUserIdentity> emailAdressResult =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL, emailId.toLowerCase());
        addValidator(errors, !(emailAdressResult.size() == 0), ParameterConstants.PARAM_USER_EMAIL_ID,
            MessageCodeConstants.AU0023, emailId, ParameterConstants.EMAIL_ADDRESS);
        LazyList<AJEntityUserIdentity> results =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_CANONICAL_USERNAME, username.toLowerCase());
        addValidator(errors, !(results.size() == 0), ParameterConstants.PARAM_USER_USERNAME,
            MessageCodeConstants.AU0023, username, ParameterConstants.PARAM_USER_USERNAME);
        rejectError(errors, HttpConstants.HttpStatus.CONFLICT.getCode());
        return new ActionResponseDTO<>(user, errors);
    }

    private AJEntityUserIdentity createUserIdentityValue(final UserDTO userDTO, final AJEntityUser user,
        final String clientId) {
        final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
        userIdentity.setUsername(userDTO.getUsername());
        userIdentity.setCanonicalUsername(userDTO.getUsername().toLowerCase());
        if (user.getEmailId() != null) {
            userIdentity.setEmailId(user.getEmailId());
        }
        userIdentity.setUserId(user.getId());
        userIdentity.setLoginType(HelperConstants.UserIdentityLoginType.CREDENTIAL.getType());
        userIdentity.setProvisionType(HelperConstants.UserIdentityProvisionType.REGISTERED.getType());
        userIdentity.setPassword(InternalHelper.encryptPassword(userDTO.getPassword()));
        userIdentity.setClientId(clientId);
        userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
        return userIdentity;
    }

    private void saveAccessToken(final String token, final JsonObject accessToken, final Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
}
