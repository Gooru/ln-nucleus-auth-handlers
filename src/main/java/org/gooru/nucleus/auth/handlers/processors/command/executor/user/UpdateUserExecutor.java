package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.javalite.activejdbc.LazyList;

public final class UpdateUserExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private UserDTO userDTO;
    private String userId;
    private AJEntityUser user;
    private EventBuilder eventBuilder;

    public UpdateUserExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
        if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
            userId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
        }
        userDTO = new UserDTO(messageContext.requestBody());

    }

    @Override
    public void validateRequest() {
        ActionResponseDTO<AJEntityUser> userValidator = updateUserValidator();
        user = userValidator.getModel();
        eventBuilder = userValidator.getEventBuilder();
    }

    @Override
    public MessageResponse executeRequest() {
        user.saveIt();
        eventBuilder.setEventName(Event.UPDATE_USER.getName());

        eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, HelperConstants.USERS_JSON_FIELDS).toJson(user));
        if (userDTO.getUsername() != null) {
            LazyList<AJEntityUserIdentity> results =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_USER_ID, userId);
            AJEntityUserIdentity userIdentity = results.get(0);
            userIdentity.setUsername(userDTO.getUsername());
            userIdentity.setCanonicalUsername(userDTO.getUsername().toLowerCase());
            userIdentity.saveIt();
            eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY,
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, null).toJson(userIdentity));
        }
        return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build())
            .setStatusNoOutput().successful().build();
    }

    private ActionResponseDTO<AJEntityUser> updateUserValidator() {
        LazyList<AJEntityUser> users = AJEntityUser.where(AJEntityUser.GET_USER, userId);
        final AJEntityUser user = users.size() > 0 ? users.get(0) : null;
        rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
        LazyList<AJEntityUserIdentity> userIdentitys =
            AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_USER_ID, userId);
        final AJEntityUserIdentity userIdentity = userIdentitys.size() > 0 ? userIdentitys.get(0) : null;
        rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
        reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED),
            MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
        final JsonObject errors = new JsonObject();
        final String username = userDTO.getUsername();
        final EventBuilder eventBuilder = new EventBuilder();

        if (userDTO.getFirstname() != null) {
            addValidator(errors, !(userDTO.getFirstname().matches("[a-zA-Z0-9'. -]+")),
                ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021,
                ParameterConstants.PARAM_USER_FIRSTNAME);
            user.setFirstname(userDTO.getFirstname());
        }
        if (userDTO.getLastname() != null) {
            addValidator(errors, !(userDTO.getLastname().matches("[a-zA-Z0-9'. -]+")),
                ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021,
                ParameterConstants.PARAM_USER_LASTNAME);
            user.setLastname(userDTO.getLastname());
        }
        if (userDTO.getGender() != null) {
            addValidator(errors, (HelperConstants.USER_GENDER.get(userDTO.getGender()) == null),
                ParameterConstants.PARAM_USER_GENDER, MessageCodeConstants.AU0024);
            user.setGender(userDTO.getGender());
        }
        if (userDTO.getUserCategory() != null) {
            addValidator(errors, (HelperConstants.USER_CATEGORY.get(userDTO.getUserCategory()) == null),
                ParameterConstants.PARAM_USER_CATEGORY, MessageCodeConstants.AU0025);
            user.setUserCategory(userDTO.getUserCategory());
        }
        if (username != null) {
            addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME,
                MessageCodeConstants.AU0017);
            addValidator(errors, ((username.length() < 4 || username.length() > 20)),
                ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
                ParameterConstants.PARAM_USER_USERNAME, "4", "20");
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

        if (userDTO.getStateId() != null && !userDTO.getStateId().isEmpty()) {
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
            user.setStateId(null);
        } else if (userDTO.getStateId() != null && userDTO.getStateId().isEmpty()) {
            user.setStateId(null);
            user.setState(null);
        }

        if (userDTO.getSchoolDistrictId() != null && !userDTO.getSchoolDistrictId().isEmpty()) {
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
            user.setSchoolDistrictId(null);
        } else if (userDTO.getSchoolDistrictId() != null && userDTO.getSchoolDistrictId().isEmpty()) {
            user.setSchoolDistrictId(null);
            user.setSchoolDistrict(null);
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

        if (userDTO.getGrade() != null) {
            user.setGrade(userDTO.getGrade());
        }

        if (userDTO.getCourse() != null) {
            user.setCourse(userDTO.getCourse());
        }
        if (userDTO.getAboutMe() != null) {
            user.setAboutMe(userDTO.getAboutMe());
        }
        if (userDTO.getThumbnailPath() != null) {
            user.setThumbnailPath(userDTO.getThumbnailPath());
        }

        if (userDTO.getRosterGlobalUserId() != null) {
            user.setRosterGlobalUserId(userDTO.getRosterGlobalUserId());
        }
        rejectError(errors, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
        if (username != null) {
            LazyList<AJEntityUserIdentity> results =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_CANONICAL_USERNAME, username.toLowerCase());
            addValidator(errors, !(results.size() == 0), ParameterConstants.PARAM_USER_USERNAME,
                MessageCodeConstants.AU0023, username, ParameterConstants.PARAM_USER_USERNAME);
        }
        rejectError(errors, HttpConstants.HttpStatus.CONFLICT.getCode());
        return new ActionResponseDTO<>(user, eventBuilder, errors);

    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
}
