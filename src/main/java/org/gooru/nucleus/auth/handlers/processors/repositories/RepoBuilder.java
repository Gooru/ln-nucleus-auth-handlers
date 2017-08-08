package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJRepoBuilder;

/**
 * @author szgooru
 *         Created On: 02-Jan-2017
 */
public final class RepoBuilder {

    private RepoBuilder() {
        throw new AssertionError();
    }

    public static AuthenticationRepo buildAuthenticationRepo(ProcessorContext context) {
        return AJRepoBuilder.buildAuthenticationRepo(context);
    }

    public static UserRepo buildUserRepo(ProcessorContext context) {
        return AJRepoBuilder.buildUserRepo(context);
    }

    public static InternalRepo buildInternalRepo(ProcessorContext context) {
        return AJRepoBuilder.buildInternalRepo(context);
    }
}
