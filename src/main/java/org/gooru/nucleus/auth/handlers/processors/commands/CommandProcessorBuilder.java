package org.gooru.nucleus.auth.handlers.processors.commands;

import java.util.HashMap;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.processors.Processor;
import org.gooru.nucleus.auth.handlers.processors.ProcessorContext;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru
 *         Created On: 30-Dec-2016
 */
public enum CommandProcessorBuilder {

    DEFAULT("default") {
        private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);

        public Processor build(ProcessorContext context) {
            return () -> {
                LOGGER.debug("Invalid operation type passed in, not able to handle");
                throw new InvalidRequestException();
            };
        }
    },
    // TODO: Need to create instances of Internal Processors as well
    ANONYMOUS_SIGNIN(MessageConstants.MSG_OP_ANONYMOUS_SIGNIN) {
        @Override
        public Processor build(ProcessorContext context) {
            return new SigninAnonymousProcessor(context);
        }
    },
    USER_SIGNIN(MessageConstants.MSG_OP_USER_SIGNIN) {
        @Override
        public Processor build(ProcessorContext context) {
            return new SigninUserProcessor(context);
        }
    },
    USER_SIGNUP(MessageConstants.MSG_OP_USER_SIGNUP) {
        @Override
        public Processor build(ProcessorContext context) {
            return new SignupUserProcessor(context);
        }
    },
    USER_UPDATE(MessageConstants.MSG_OP_USER_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UpdateUserProcessor(context);
        }
    },
    USER_TOKEN_CHECK(MessageConstants.MSG_OP_USER_TOKEN_CHECK) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CheckUserTokenProcessor(context);
        }
    },
    USER_TOKEN_DETAILS(MessageConstants.MSG_OP_USER_TOKEN_DETAILS) {
        @Override
        public Processor build(ProcessorContext context) {
            return new GetUserTokenDetailsProcessor(context);
        }
    },
    USER_SIGNOUT(MessageConstants.MSG_OP_USER_SIGNOUT) {
        @Override
        public Processor build(ProcessorContext context) {
            return new SignoutUserProcessor(context);
        }
    },
    USER_AUTHORIZE(MessageConstants.MSG_OP_USER_AUTHORIZE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new AuthorizeUserProcessor(context);
        }
    },
    USER_PASSWORD_RESET_TRIGGER(MessageConstants.MSG_OP_USER_PASSWORD_RESET_TRIGGER) {
        @Override
        public Processor build(ProcessorContext context) {
            return new TriggerResetPasswordForUserProcessor(context);
        }
    },
    USER_PASSWORD_RESET(MessageConstants.MSG_OP_USER_PASSWORD_RESET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResetPasswordProcessor(context);
        }
    },
    USER_PASSWORD_CHANGE(MessageConstants.MSG_OP_USER_PASSWORD_CHANGE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ChangePasswordProcessor(context);
        }
    };

    private String name;

    CommandProcessorBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

    static {
        for (CommandProcessorBuilder builder : values()) {
            LOOKUP.put(builder.name, builder);
        }
    }

    public static CommandProcessorBuilder lookupBuilder(String name) {
        CommandProcessorBuilder builder = LOOKUP.get(name);
        if (builder == null) {
            return DEFAULT;
        }
        return builder;
    }

    public abstract Processor build(ProcessorContext context);
}
