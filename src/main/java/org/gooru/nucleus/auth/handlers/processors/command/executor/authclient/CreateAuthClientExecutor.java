package org.gooru.nucleus.auth.handlers.processors.command.executor.authclient;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPermission;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class CreateAuthClientExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private AuthClientDTO authClientDTO;
    private String userId;
    private static Logger LOGGER = LoggerFactory.getLogger(CreateAuthClientExecutor.class);

    public CreateAuthClientExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        userId = messageContext.user().getUserId();
    }

    @Override
    public void validateRequest() {
        authClientDTO = new AuthClientDTO(messageContext.requestBody());
        ServerValidatorUtility.rejectIfNull(authClientDTO.getName(), MessageCodeConstants.AU0047, 400);
        ServerValidatorUtility.rejectIfNull(authClientDTO.getGrantTypes(), MessageCodeConstants.AU0048, 400);
        ServerValidatorUtility.rejectIfNull(authClientDTO.getCdnUrls(), MessageCodeConstants.AU0049, 400);
        LazyList<AJEntityUserPermission> permissions =
            AJEntityUserPermission.where(AJEntityUserPermission.GET_USER_PERMISSION, userId);
        AJEntityUserPermission permission = permissions.size() > 0 ? permissions.get(0) : null;
        ServerValidatorUtility.reject((permission == null), MessageCodeConstants.AU0050, 403);
        JsonObject jsonPermission = permission.getPermission();
        ServerValidatorUtility.reject((jsonPermission == null), MessageCodeConstants.AU0050, 403);
        ServerValidatorUtility.reject((jsonPermission.getBoolean(HelperConstants.CREATE_APP_KEY) == null || !jsonPermission.getBoolean(HelperConstants.CREATE_APP_KEY)),
            MessageCodeConstants.AU0050, 403);
    }

    @Override
    public MessageResponse executeRequest() {
        
        List<Map> uuid = Base.findAll(AJEntityAuthClient.SELECT_CLIENT_UUID);
        String clientId = uuid.get(0).get(AJEntityAuthClient.UUID).toString(); 
        LOGGER.debug("Client ID generated from DB: {}", clientId);
        
        long createdAt = System.currentTimeMillis();
        LOGGER.debug("Created At: {}", createdAt);
        
        String clientKey = InternalHelper.encrypt(clientId + AJEntityAuthClient.CLIENT_KEY_SEPARATOR + createdAt);
        LOGGER.debug("Client Key generated: {}", clientKey);
        
        int cnt = Base.exec(AJEntityAuthClient.INSERT_AUTH_CLIENT, clientId, authClientDTO.getName(),
            authClientDTO.getUrl(), InternalHelper.encryptClientKey(clientKey), authClientDTO.getDescription(), authClientDTO.getContactEmail(), 3600, new Timestamp(createdAt),
            toPostgresArrayString(authClientDTO.getGrantTypes()), authClientDTO.getCdnUrls().toString());

        if (cnt == 0) {
            JsonObject result = new JsonObject();
            result.put("message", "invalid request parameters");
            return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusBadRequest().failed().build();
        }
        
        JsonObject results = new JsonObject();
        results.put(ParameterConstants.PARAM_CLIENT_ID, clientId);
        results.put(ParameterConstants.PARAM_CLIENT_KEY, clientKey);
        return new MessageResponse.Builder().setResponseBody(results).setContentTypeJson().setStatusOkay().successful()
            .build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
    
    public static String toPostgresArrayString(JsonArray input) {
        Iterator<Object> it = input.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            String s = it.next().toString();
            sb.append('"').append(s).append('"');
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',');
        }
    }

}
