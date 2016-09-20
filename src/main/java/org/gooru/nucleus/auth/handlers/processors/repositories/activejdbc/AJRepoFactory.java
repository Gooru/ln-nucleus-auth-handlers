package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationGLARepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthorizeRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.InternalRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserPrefsRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;

public final class AJRepoFactory {

    public static AuthenticationGLARepo getAuthenticationGLARepo(MessageContext messageContext) {
        return new AJAuthenticationGLARepo(messageContext);
    }

    public static AuthenticationRepo getAuthenticationRepo(MessageContext messageContext) {
        return new AJAuthenticationRepo(messageContext);
    }

    public static UserRepo getUserRepo(MessageContext messageContext) {
        return new AJUserRepo(messageContext);
    }

    public static UserPrefsRepo getUserPrefsRepo(MessageContext messageContext) {
        return new AJUserPrefsRepo(messageContext);
    }

    public static AuthorizeRepo getAuthorizeRepo(MessageContext messageContext) {
        return new AJAuthorizeRepo(messageContext);
    }

    public static AuthClientRepo getAuthClientRepo(MessageContext messageContext) {
        return new AJAuthClientRepo(messageContext);
    }
    
    public static InternalRepo getInternalRepo(MessageContext messageContext) {
        return new AJInternlaRepo(messageContext);
    }

    private AJRepoFactory() {
        throw new AssertionError();
    }
}
