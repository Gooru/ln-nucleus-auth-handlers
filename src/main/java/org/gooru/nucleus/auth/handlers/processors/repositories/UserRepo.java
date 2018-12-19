package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author gooru Created On: 02-Jan-2017
 */
public interface UserRepo {

  MessageResponse signupUser();

  MessageResponse authorizeUser();

  MessageResponse updateUser();

  MessageResponse triggerResetPassword();

  MessageResponse resetPassword();

  MessageResponse changePassword();
}
