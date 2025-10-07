package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Represents a collection of predefined error codes and associated metadata used for providing
 * consistent error handling within the application.
 *
 * <p>Each error code is associated with: - A corresponding {@link HttpStatus} to align with HTTP
 * response standards. - A default message to describe the specific error scenario.
 *
 * <p>This enum centralizes error management, enabling improved maintainability and standardized
 * error responses across the application.
 *
 * <p>Constants: - PAYLOAD_TOO_LARGE: Indicates that the file size exceeds the maximum allowed
 * limit. - MISSING_REQUEST_PART: Denotes a missing part in the request, such as a required form
 * data element. - CHECKSUM_MISMATCH: Represents an error where generating the file checksum failed.
 * - DOCUMENT_NOT_FOUND: Indicates that the requested document does not exist. - DOWNLOAD_ERROR:
 * Represents an error retrieving a document from the storage system. - DOCUMENT_NOT_CREATED:
 * Denotes an issue where a document could not be created. - INTERNAL_ERROR: Represents an
 * unexpected server error.
 *
 * <p>Constructor: - {@code ErrorCode(HttpStatus status, String defaultMessage)}: Initializes the
 * enum with an HTTP status and a corresponding default error message.
 */
@Getter
public enum ErrorCode {
  PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds the maximum allowed size."),
  MISSING_REQUEST_PART(HttpStatus.BAD_REQUEST, "Required part '%s' is not present."),
  CHECKSUM_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate file checksum."),
  DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested document was not found."),
  DOWNLOAD_ERROR(HttpStatus.BAD_GATEWAY, "Error retrieving the document from MinIO storage."),
  DOCUMENT_NOT_CREATED(HttpStatus.INTERNAL_SERVER_ERROR, "Document not created"),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");

  private final HttpStatus status;
  private final String defaultMessage;

  ErrorCode(HttpStatus status, String defaultMessage) {
    this.status = status;
    this.defaultMessage = defaultMessage;
  }
}
