package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public class AJAuthenticationRepo implements AuthenticationRepo {

  private final ProcessorContext context;

  public AJAuthenticationRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse signinAnonymous() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildSigninAnonymousHandler(context));
  }

  @Override
  public MessageResponse signinUser() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildSigninUserHandler(context));
  }

  @Override
  public MessageResponse getAccessTokenDetails() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildGetAccessTokenDetailsHandler(context));
  }

  @Override
  public MessageResponse domainBasedRedirect() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildDomainBasedRedirectHandler(context));
  }

  @Override
  public MessageResponse initLogin() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildInitLoginHandler(context));
  }

  @Override
  public MessageResponse signout() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildSignoutHandler(context));
  }

  @Override
  public MessageResponse createAccessTokenUsingRefreshToken() {
    return new TransactionExecutor().executeTransaction(
        DBHandlerBuilder.buildCreateAccessTokenUsingRefreshTokenHandler(context));
  }

  @Override
  public MessageResponse revokeRefreshToken() {
    return new TransactionExecutor().executeTransaction(
        DBHandlerBuilder.buildRevokeRefreshTokenHandler(context));
  }
}
