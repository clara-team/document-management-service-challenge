package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

import java.time.Instant;

/**
 * Represents an error response containing details about an error that occurred within the
 * application.
 *
 * <p>The ErrorResponse record encapsulates the following elements: - `code`: A string representing
 * the error code, typically associated with specific error scenarios. - `message`: A detailed
 * message providing context or explanation about the error. - `timestamp`: The time when the error
 * occurred
 */
public record ErrorResponse(String code, String message, String timestamp) {
  public ErrorResponse(String code, String message) {
    this(code, message, Instant.now().toString());
  }
}
