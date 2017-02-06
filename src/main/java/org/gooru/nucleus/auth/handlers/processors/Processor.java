package org.gooru.nucleus.auth.handlers.processors;

import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;

/**
 * @author szgooru
 *         Created On: 30-Dec-2016
 */
public interface Processor {

    MessageResponse process();
}
