package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception that extends {@link RuntimeException} and is thrown to indicate validation
 * errors encountered during document processing.
 *
 * <p>The {@code DocumentValidationException} class provides detailed information about the
 * validation issue by including the message, the user ID, and the file name associated with the
 * error.
 *
 * <p>This exception is annotated with {@code @ResponseStatus} to map it to a specific HTTP status
 * code (400 BAD REQUEST), which will be returned when this exception is thrown and not handled
 * explicitly.
 *
 * <p>Constructor: - {@code DocumentValidationException(String message, String userId, String
 * fileName)}: Creates an instance of the exception with a custom error message incorporating the
 * user ID and file name.
 */
@ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
public class DocumentValidationException extends RuntimeException {
  public DocumentValidationException(String message, String userId, String fileName) {
    super(String.format("%s (userId=%s, fileName=%s)", message, userId, fileName));
  }
}
