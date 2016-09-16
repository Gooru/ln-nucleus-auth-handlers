package org.gooru.nucleus.auth.handlers.processors.command.executor.internal;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class InternalExecutorFactory {

	public static DBExecutor authenticate(MessageContext messageContext) {
		return new AuthenticateExecutor(messageContext);
	}

	public static DBExecutor impersonate(MessageContext messageContext) {
		return new ImpersonateExecutor(messageContext);
	}

}
