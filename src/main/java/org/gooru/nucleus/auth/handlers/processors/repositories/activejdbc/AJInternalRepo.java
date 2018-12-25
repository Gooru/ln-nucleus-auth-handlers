package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.InternalRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author szgooru Created On: 03-Jan-2017
 */
public class AJInternalRepo implements InternalRepo {

  private final ProcessorContext context;

  public AJInternalRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse authenticate() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildInternalAuthenticateHandler(context));
  }

  @Override
  public MessageResponse impersonate() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildInternalImpersonateHadler(context));
  }

  @Override
  public MessageResponse ltisso() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildInternalLtiSSOHandler(context));
  }

  @Override
  public MessageResponse wsfedsso() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildInternalWSFedSSOHandler(context));
  }

  @Override
  public MessageResponse tenantRealm() {
    return new TransactionExecutor()
        .executeTransaction(DBHandlerBuilder.buildTenantRealmHandler(context));
  }

}
