package org.gooru.nucleus.auth.handlers.processors.responses;

import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants.HttpStatus;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.processors.events.EventBuilder;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 6/1/16.
 */
public final class MessageResponseFactory {

    private static final String API_VERSION_DEPRECATED = "API version is deprecated";
    private static final String API_VERSION_NOT_SUPPORTED = "API version is not supported";

    private MessageResponseFactory() {
        throw new AssertionError();
    }

    public static MessageResponse createInvalidRequestResponse() {
        return new MessageResponse.Builder().failed().setStatusBadRequest().build();
    }

    public static MessageResponse createInvalidRequestResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusBadRequest().setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createForbiddenResponse() {
        return new MessageResponse.Builder().failed().setStatusForbidden().build();
    }

    public static MessageResponse createForbiddenResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusForbidden()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createUnauthorizedResponse() {
        return new MessageResponse.Builder().failed().setStatusUnauthorized().build();
    }

    public static MessageResponse createUnauthorizedResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusUnauthorized()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createInternalErrorResponse() {
        return new MessageResponse.Builder().failed().setStatusInternalError().build();
    }

    public static MessageResponse createInternalErrorResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusInternalError().setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createNotFoundResponse() {
        return new MessageResponse.Builder().failed().setStatusNotFound().build();
    }

    public static MessageResponse createNotFoundResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusNotFound().setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createGetResponse(JsonObject responseBody) {
        return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson()
            .setResponseBody(responseBody).build();
    }
    
    public static MessageResponse createGetResponse(JsonObject responseBody, EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson()
            .setResponseBody(responseBody).setEventData(eventBuilder.build()).build();
    }

    public static MessageResponse createConflictRespose() {
        return new MessageResponse.Builder().failed().setStatusHttpCode(HttpStatus.CONFLICT).build();
    }

    public static MessageResponse createPostResponse(String location) {
        return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusCreated().build();
    }

    public static MessageResponse createPostResponse(JsonObject response, EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setStatusCreated().setContentTypeJson()
            .setResponseBody(response).setEventData(eventBuilder.build()).build();
    }

    public static MessageResponse createPostResponse(EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setStatusCreated().setEventData(eventBuilder.build()).build();
    }

    public static MessageResponse createNoContentResponse() {
        return new MessageResponse.Builder().successful().setStatusNoOutput().build();
    }

    public static MessageResponse createNoContentResponse(EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setStatusNoOutput().setEventData(eventBuilder.build())
            .build();
    }

    public static MessageResponse createValidationErrorResponse(JsonObject errors) {
        return new MessageResponse.Builder().validationFailed().setStatusBadRequest().setContentTypeJson()
            .setResponseBody(errors).build();
    }

    public static MessageResponse createVersionDeprecatedResponse() {
        return new MessageResponse.Builder().failed().setStatusHttpCode(HttpConstants.HttpStatus.GONE)
            .setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, API_VERSION_DEPRECATED)).build();
    }

    public static MessageResponse createGoneResponse() {
        return new MessageResponse.Builder().failed().setStatusHttpCode(HttpStatus.GONE).build();
    }
}
