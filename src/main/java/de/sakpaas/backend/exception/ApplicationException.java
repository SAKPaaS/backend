package de.sakpaas.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception that will be used by the Expect class in order to throw errors throughout the whole
 * application. It is possible to create new exceptions based on extending this class
 */
public class ApplicationException extends ResponseStatusException {

  private static final long serialVersionUID = 1L;

  @Getter
  private boolean internal;

  @Getter
  private String textId;

  @Getter
  private Object[] replacers;


  /**
   * Creates an ApplicationException with the given values.
   * 
   * @param status the response status that will be send
   * @param message the message that will be in the log
   * @param internal true if there should be a standardized message to the frontend
   * @param textId the textId of the error for the frontend
   * @param replacers replacers for the error message
   */
  public ApplicationException(HttpStatus status, String message, boolean internal, String textId,
      Object... replacers) {
    super(status, message);
    this.internal = internal;
    this.textId = textId;
    this.replacers = replacers;
  }
}
