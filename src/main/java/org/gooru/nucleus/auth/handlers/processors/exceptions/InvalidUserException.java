package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class InvalidUserException extends RuntimeException {

    private static final long serialVersionUID = 216475736594474856L;

    public InvalidUserException() {
    }

    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException(Throwable cause) {
        super(cause);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause);
    }

}
