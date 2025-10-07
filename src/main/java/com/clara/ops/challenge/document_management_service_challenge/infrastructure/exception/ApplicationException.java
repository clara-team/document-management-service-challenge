package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

/**
 * A custom exception that extends {@link RuntimeException} and is used for handling
 * application-specific errors within the system.
 *
 * <p>The {@code ApplicationException} provides a mechanism to associate a specific error code,
 * represented by the {@link ErrorCode} enum, with the exception instance. This enables standardized
 * error management and allows for consistent error responses across the application.
 *
 * <p>Constructors: - {@code ApplicationException(ErrorCode errorCode)}: Initializes the exception
 * with a specific error code. The exception message is set to the default message associated with
 * the provided error code. - {@code ApplicationException(ErrorCode errorCode, String message)}:
 * Initializes the exception with a specific error code and a custom error message. - {@code
 * ApplicationException(ErrorCode errorCode, String message, Throwable cause)}: Initializes the
 * exception with a specific error code, a custom message, and a throwable cause for additional
 * error context.
 */
public class ApplicationException extends RuntimeException {
  private final ErrorCode errorCode;

  public ApplicationException(ErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
