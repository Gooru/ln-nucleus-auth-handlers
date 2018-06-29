package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityDomainBasedRedirect;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru on 28-Jun-2018
 */
public class InitLoginHandler implements DBHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitLoginHandler.class);
	private final ProcessorContext context;
	private AJEntityDomainBasedRedirect redirectModel;
	
	private String classCode = null;
	private String contextUrl = null;

	public InitLoginHandler(ProcessorContext context) {
		this.context = context;
	}

	@Override
	public ExecutionResult<MessageResponse> checkSanity() {
		this.classCode = this.context.requestBody().getString("class_code");
		this.contextUrl = this.context.requestBody().getString("context_url");

		if (this.classCode == null || this.classCode.isEmpty()) {
			LOGGER.warn("Invalid class code provided");
			return new ExecutionResult<>(
					MessageResponseFactory.createInvalidRequestResponse("invalid class code provided"),
					ExecutionResult.ExecutionStatus.FAILED);
		}

		return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
	}

	@Override
	public ExecutionResult<MessageResponse> validateRequest() {
		
		Object o = Base.firstCell(
				"SELECT tenant FROM class WHERE code = ? AND class_sharing = 'restricted' AND is_archived = false AND is_deleted = false",
				this.classCode);
		if (o == null) {
			LOGGER.debug("no class found with class code {}", this.classCode);
			return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("class not found"),
					ExecutionResult.ExecutionStatus.FAILED);
		}

		UUID tenant = (UUID) o;

		this.redirectModel = AJEntityDomainBasedRedirect.findFirst(AJEntityDomainBasedRedirect.FIND_BY_TENANT, tenant);

		return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
	}

	@Override
	public ExecutionResult<MessageResponse> executeRequest() {
		JsonObject response = new JsonObject();
		if (this.redirectModel == null) {
			response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE, HttpConstants.HttpStatus.NOT_FOUND.getCode());
			response.putNull(AJEntityDomainBasedRedirect.REDIRECT_URL);
		} else {
			String redirectUrl = this.redirectModel.getString(AJEntityDomainBasedRedirect.REDIRECT_URL);
			if (redirectUrl == null) {
				response.putNull(AJEntityDomainBasedRedirect.REDIRECT_URL);
				response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE, HttpConstants.HttpStatus.SUCCESS.getCode());
			} else {
				try {
					redirectUrl = redirectUrl.concat(
							(this.contextUrl != null ? "?context=" + URLEncoder.encode(contextUrl, "UTF-8") : ""));
				} catch (UnsupportedEncodingException e) {
					LOGGER.warn("unable to encode URL, using unencoded URL");
					redirectUrl = redirectUrl.concat((this.contextUrl != null ? "?context=" + contextUrl : ""));
				}
				response.put(AJEntityDomainBasedRedirect.RESP_STATUS_CODE,
						HttpConstants.HttpStatus.SEE_OTHER.getCode());
				response.put(AJEntityDomainBasedRedirect.REDIRECT_URL, redirectUrl);
			}
		}
		
		return new ExecutionResult<>(MessageResponseFactory.createGetResponse(response),
				ExecutionResult.ExecutionStatus.SUCCESSFUL);
	}

	@Override
	public boolean handlerReadOnly() {
		return false;
	}

}
