package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author szgooru
 *         Created On: 02-Jan-2017
 */
public class AJUserRepo implements UserRepo {

    private final ProcessorContext context;

    public AJUserRepo(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageResponse signupUser() {
        return new TransactionExecutor().executeTransaction(DBHandlerBuilder.buildSignupUserHandler(context));
    }

    @Override
    public MessageResponse updateUser() {
        return new TransactionExecutor().executeTransaction(DBHandlerBuilder.buildUpdateUserHandler(context));
    }

    @Override
    public MessageResponse authorizeUser() {
        return new TransactionExecutor().executeTransaction(DBHandlerBuilder.buildAuthorizeUserHandler(context));
    }

    @Override
    public MessageResponse triggerResetPassword() {
        return new TransactionExecutor()
            .executeTransaction(DBHandlerBuilder.buildTriggerResetPasswordHandler(context));
    }

    @Override
    public MessageResponse resetPassword() {
        return new TransactionExecutor().executeTransaction(DBHandlerBuilder.buildResetPassowrdHandler(context));
    }

    @Override
    public MessageResponse changePassword() {
        return new TransactionExecutor().executeTransaction(DBHandlerBuilder.buildChangePasswordHandler(context));
    }

}
