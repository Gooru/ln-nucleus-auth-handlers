package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityDomainBasedRedirect;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUsers;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 23-Oct-2017
 */
public class DomainBasedRedirectHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainBasedRedirectHandler.class);
    private final ProcessorContext context;
    AJEntityDomainBasedRedirect domainBasedRedirectURL;

    public DomainBasedRedirectHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.requestBody(),
            AJEntityDomainBasedRedirect.fieldSelector(), AJEntityUsers.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        initializeDomainBasedRedirectModel();

        JsonObject response = generateResponse();

        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(response),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    private JsonObject generateResponse() {
        JsonObject response = new JsonObject();
        if (this.domainBasedRedirectURL == null) {
            response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE, HttpConstants.HttpStatus.NOT_FOUND.getCode());
            response.putNull(AJEntityDomainBasedRedirect.REDIRECT_URL);
        } else {
            String redirectUrl = this.domainBasedRedirectURL.getString(AJEntityDomainBasedRedirect.REDIRECT_URL);
            if (redirectUrl == null ) {
                response.putNull(AJEntityDomainBasedRedirect.REDIRECT_URL);
                response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE, HttpConstants.HttpStatus.SUCCESS.getCode());
            } else {
                response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE, HttpConstants.HttpStatus.SEE_OTHER.getCode());
                response.put(AJEntityDomainBasedRedirect.REDIRECT_URL, redirectUrl);
            }
        }
        return response;
    }

    private void initializeDomainBasedRedirectModel() {
        String domain = getDomain();
        this.domainBasedRedirectURL =
            AJEntityDomainBasedRedirect.findFirst(AJEntityDomainBasedRedirect.FIND_BY_DOMAIN, domain);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    public String getDomain() {
        String domain = context.requestBody().getString(AJEntityDomainBasedRedirect.DOMAIN);
        if (domain.startsWith("www.") || domain.startsWith("WWW.")) {
            domain = domain.substring(4);
        }

        return domain.toLowerCase();
    }
}
