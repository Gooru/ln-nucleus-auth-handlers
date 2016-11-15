package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.internal.InternalExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.InternalRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJInternlaRepo implements InternalRepo {

    private final MessageContext messageContext;
    
    public AJInternlaRepo(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public MessageResponse authenticate() {
        return TransactionExecutor.executeTransaction(InternalExecutorFactory.authenticate(messageContext));
    }

    @Override
    public MessageResponse impersonate() {
        return TransactionExecutor.executeTransaction(InternalExecutorFactory.impersonate(messageContext));
    }

    @Override
    public MessageResponse sso() {
        return TransactionExecutor.executeTransaction(InternalExecutorFactory.sso(messageContext));
    }

}
