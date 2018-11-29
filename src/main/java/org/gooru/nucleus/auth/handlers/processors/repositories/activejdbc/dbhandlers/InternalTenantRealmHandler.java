package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.app.components.AppConfiguration;
import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalTenantRealmHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalTenantRealmHandler.class);
    private final ProcessorContext context;
    private String shortName;
    private final RedisClient redisClient;
    private final String appLoginUrl;
    
    public InternalTenantRealmHandler(ProcessorContext context) {
        this.context = context;
        this.redisClient = RedisClient.instance();
        this.appLoginUrl = AppConfiguration.getInstance().appLoginUrl();
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        shortName = context.headers().get(ParameterConstants.PARAM_SHORT_NAME);
        if (shortName == null || shortName.isEmpty()) {
            LOGGER.warn("shortname is null or empty.");
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        LazyList<AJEntityTenant> tenants = AJEntityTenant.findBySQL(AJEntityTenant.SELECT_BY_SHORT_NAME_GRANT_TYPE,
            shortName, HelperConstants.GrantTypes.credential.getType());
        if (tenants.size() > 0) {
            final AJEntityTenant tenant = tenants.get(0);
            String nonce = generateNonceAndsaveInRedis(tenant.getString(AJEntityTenant.ID));
            String appLoginUrl = this.appLoginUrl + "?nonce=" + nonce;
            return new ExecutionResult<>(MessageResponseFactory.createMovePermanentlyResponse(appLoginUrl),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        } else {
            LOGGER.warn("No tenant match with this shortname {}.", shortName);
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }
    }

    private String generateNonceAndsaveInRedis(String tenantId) {
        String nonce = UUID.randomUUID().toString();
        int nonceExpireInSecs = this.redisClient.getNonceExpireInSecs();
        this.redisClient.set(nonce, tenantId, nonceExpireInSecs);
        return nonce;
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
