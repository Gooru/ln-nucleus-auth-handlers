package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityApp;

/**
 * @author szgooru Created On: 17-May-2017
 */
public final class AuthorizerBuilder {

  private AuthorizerBuilder() {
    throw new AssertionError();
  }

  public static Authorizer<AJEntityApp> buildAppAuthorizer(ProcessorContext context) {
    return new AppAuthorizer(context);
  }
}
