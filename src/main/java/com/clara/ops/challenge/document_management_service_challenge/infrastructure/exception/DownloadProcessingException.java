package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception that indicates an error occurred during the processing of a download
 * operation.
 *
 * <p>The {@code DownloadProcessingException} extends {@link RuntimeException} and is primarily
 * meant for capturing and representing issues specifically related to file download processing
 * within the application.
 *
 * <p>Typical usage includes scenarios where a download operation fails unexpectedly, such as due to
 * storage errors, checksum mismatches, or internal server failures, requiring the application to
 * respond with an appropriate error to the client.
 *
 * <p>This exception is annotated with {@link ResponseStatus} to provide a default HTTP status code
 * of {@code HttpStatus.INTERNAL_SERVER_ERROR}, ensuring that clients receive consistent error
 * responses for such failures.
 *
 * <p>Constructors: - {@code DownloadProcessingException(String message, Throwable cause)}:
 * Initializes the exception with a specific error message and an optional {@code Throwable} cause,
 * providing context about the failure.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DownloadProcessingException extends RuntimeException {
  public DownloadProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
