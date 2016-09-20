package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJRepoFactory;

public final class RepoFactory {

    public static UserRepo getUserRepo(MessageContext messageContext) {
        return AJRepoFactory.getUserRepo(messageContext);
    }

    public static UserPrefsRepo getUserPrefsRepo(MessageContext messageContext) {
        return AJRepoFactory.getUserPrefsRepo(messageContext);
    }

    public static AuthenticationRepo getAuthenticationRepo(MessageContext messageContext) {
        return AJRepoFactory.getAuthenticationRepo(messageContext);
    }

    public static AuthenticationGLARepo getAuthenticationGLARepo(MessageContext messageContext) {
        return AJRepoFactory.getAuthenticationGLARepo(messageContext);
    }

    public static AuthorizeRepo getAuthorizeRepo(MessageContext messageContext) {
        return AJRepoFactory.getAuthorizeRepo(messageContext);
    }

    public static AuthClientRepo getAuthClientRepo(MessageContext messageContext) {
        return AJRepoFactory.getAuthClientRepo(messageContext);
    }
    
    public static InternalRepo getInternalRepo(MessageContext messageContext) {
        return AJRepoFactory.getInternalRepo(messageContext);
    }

    private RepoFactory() {
        throw new AssertionError();
    }
}
