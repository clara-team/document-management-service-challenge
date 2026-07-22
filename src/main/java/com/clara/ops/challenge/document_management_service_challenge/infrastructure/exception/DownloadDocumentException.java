package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception that extends {@link RuntimeException} and is thrown when an issue occurs
 * during the process of downloading a document from MinIO storage.
 *
 * <p>This exception is typically used to indicate that a document could not be retrieved due to
 * unavailability or other unexpected issues at the specified path.
 *
 * <p>The {@code @ResponseStatus} annotation with {@code HttpStatus.BAD_GATEWAY} maps this exception
 * to an HTTP 502 Bad Gateway response, signaling an issue with an upstream service or dependency.
 *
 * <p>Constructor: - {@code DownloadDocumentException(String path)}: Creates an exception with a
 * predefined
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class DownloadDocumentException extends RuntimeException {
  public DownloadDocumentException(String path) {
    super("File not found in MinIO storage at path: " + path);
  }

  public DownloadDocumentException(String message, Throwable cause) {
    super(message, cause);
  }
}
