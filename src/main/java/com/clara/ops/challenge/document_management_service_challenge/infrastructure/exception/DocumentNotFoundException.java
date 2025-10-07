package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * A custom exception that extends {@link RuntimeException} and is thrown when a requested document
 * is not found in the system.
 *
 * <p>This exception is specifically designed to handle scenarios where an operation for retrieving
 * a document is attempted but fails due to the document's absence in the database or storage.
 *
 * <p>The exception provides: - An HTTP status code (404 - Not Found) associated with the error. - A
 * detailed error message that includes the document ID to aid debugging and logging.
 *
 * <p>Constructor: - {@code DocumentNotFoundException(String documentId)}: Creates an instance of
 * this exception, including the document ID in the error message to identify the missing document.
 */
@Getter
public class DocumentNotFoundException extends RuntimeException {
  private final HttpStatus status = HttpStatus.NOT_FOUND;

  public DocumentNotFoundException(String documentId) {
    super(String.format("Document with id %s not found", documentId));
  }
}
