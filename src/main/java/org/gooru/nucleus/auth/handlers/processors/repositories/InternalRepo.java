package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;

public interface InternalRepo {

    MessageResponse authenticate();
    
    MessageResponse impersonate();
}
