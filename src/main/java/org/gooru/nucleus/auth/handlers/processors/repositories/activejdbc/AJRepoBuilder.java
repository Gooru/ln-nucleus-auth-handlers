package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.InternalRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public final class AJRepoBuilder {

  private AJRepoBuilder() {
    throw new AssertionError();
  }

  public static UserRepo buildUserRepo(ProcessorContext context) {
    return new AJUserRepo(context);
  }

  public static AuthenticationRepo buildAuthenticationRepo(ProcessorContext context) {
    return new AJAuthenticationRepo(context);
  }

  public static InternalRepo buildInternalRepo(ProcessorContext context) {
    return new AJInternalRepo(context);
  }
}
