package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author szgooru
 *         Created On: 03-Jan-2017
 */
public interface InternalRepo {

    MessageResponse authenticate();

    MessageResponse impersonate();

    MessageResponse ltisso();
    
    MessageResponse wsfedsso();
}
