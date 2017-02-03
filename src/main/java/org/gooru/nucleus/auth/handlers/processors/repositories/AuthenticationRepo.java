package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

public interface AuthenticationRepo {
    
    MessageResponse getUserTokenDetails();
    
    MessageResponse signinAnonymous();
    
    MessageResponse signinUser();
}
