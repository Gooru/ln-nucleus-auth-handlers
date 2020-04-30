package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public final class DBHandlerBuilder {

  private DBHandlerBuilder() {
    throw new AssertionError();
  }

  public static DBHandler buildSigninAnonymousHandler(ProcessorContext context) {
    return new SigninAnonymousHandler(context);
  }

  public static DBHandler buildSignupUserHandler(ProcessorContext context) {
    return new SignupUserHandler(context);
  }

  public static DBHandler buildSigninUserHandler(ProcessorContext context) {
    return new SigninUserHandler(context);
  }

  public static DBHandler buildUpdateUserHandler(ProcessorContext context) {
    return new UpdateUserHandler(context);
  }

  public static DBHandler buildAuthorizeUserHandler(ProcessorContext context) {
    return new AuthorizeUserHandler(context);
  }

  public static DBHandler buildTriggerResetPasswordHandler(ProcessorContext context) {
    return new TriggerResetPasswordHandler(context);
  }

  public static DBHandler buildResetPassowrdHandler(ProcessorContext context) {
    return new ResetPasswordHandler(context);
  }

  public static DBHandler buildInternalAuthenticateHandler(ProcessorContext context) {
    return new InternalAuthenticateHandler(context);
  }

  public static DBHandler buildInternalImpersonateHadler(ProcessorContext context) {
    return new InternalImpersonateHandler(context);
  }

  public static DBHandler buildInternalLtiSSOHandler(ProcessorContext context) {
    return new InternalLtiSSOHandler(context);
  }

  public static DBHandler buildGetAccessTokenDetailsHandler(ProcessorContext context) {
    return new GetAccessTokenDetailsHandler(context);
  }

  public static DBHandler buildChangePasswordHandler(ProcessorContext context) {
    return new ChangePassowrdHandler(context);
  }

  public static DBHandler buildInternalWSFedSSOHandler(ProcessorContext context) {
    return new InternalWSFedSSOHandler(context);
  }

  public static DBHandler buildDomainBasedRedirectHandler(ProcessorContext context) {
    return new DomainBasedRedirectHandler(context);
  }

  public static DBHandler buildInitLoginHandler(ProcessorContext context) {
    return new InitLoginHandler(context);
  }

  public static DBHandler buildTenantRealmHandler(ProcessorContext context) {
    return new InternalTenantRealmHandler(context);
  }

  public static DBHandler buildSignoutHandler(ProcessorContext context) {
    return new SignoutHandler(context);
  }

  public static DBHandler buildInternalOAuth2SSOHandler(ProcessorContext context) {
    return new InternalOAuth2SSOHandler(context);
  }

  public static DBHandler buildUserAccountsHandler(ProcessorContext context) {
    return new UserAccountsHandler(context);
  }

  public static DBHandler buildCreateAccessTokenUsingRefreshTokenHandler(ProcessorContext context) {
    return new CreateAccessTokenUsingRefreshTokenHandler(context);
  }


}
