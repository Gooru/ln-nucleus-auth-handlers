package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.auth.handlers.processors.responses.ExecutionResult;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.javalite.activejdbc.Model;

/**
 * @author szgooru Created On: 17-May-2017
 */
public interface Authorizer<T extends Model> {

  ExecutionResult<MessageResponse> authorize(T model);
}
