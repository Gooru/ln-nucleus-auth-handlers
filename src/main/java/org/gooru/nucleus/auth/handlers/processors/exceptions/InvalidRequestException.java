package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class InvalidRequestException extends RuntimeException {

  private static final long serialVersionUID = 4276973780093751708L;

  public InvalidRequestException() {
    super();
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(Throwable cause) {
    super(cause);
  }

}
