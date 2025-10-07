package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to download a document they are not authorized to access.
 *
 * <p>This exception represents a forbidden action, indicating that the user does not have the
 * necessary permissions to perform the download operation. It is annotated with
 * {@code @ResponseStatus} to automatically translate to a 403 FORBIDDEN HTTP response.
 *
 * <p>The exception message contains the user ID and document ID to provide specific details on the
 * unauthorized access attempt.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedDownloadException extends RuntimeException {
  public UnauthorizedDownloadException(String userId, String documentId) {
    super("User " + userId + " is not allowed to download document " + documentId);
  }
}
