package org.gooru.nucleus.auth.handlers.processors.commands;

import java.util.ArrayList;
import java.util.List;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.processors.Processor;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.responses.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.utils.VersionValidationUtils;

/**
 * @author ashish on 29/12/16.
 */
abstract class AbstractCommandProcessor implements Processor {
    protected List<String> deprecatedVersions = new ArrayList<>();
    protected final ProcessorContext context;
    protected String version;

    protected AbstractCommandProcessor(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageResponse process() {
        setDeprecatedVersions();
        if (!context.operation().equalsIgnoreCase(MessageConstants.MSG_OP_ACCESS_TOKEN_CHECK)) {
            version = VersionValidationUtils.validateVersion(deprecatedVersions, context.headers());
        }
        return processCommand();
    }

    protected abstract void setDeprecatedVersions();

    protected abstract MessageResponse processCommand();
}
