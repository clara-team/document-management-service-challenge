package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

/**
 * A custom exception that extends {@code RuntimeException} and is thrown to indicate checksum
 * related errors in the application.
 *
 * <p>The {@code ChecksumException} is primarily used in scenarios where checksum validation fails
 * or errors occur while generating or comparing checksums for files.
 *
 * <p>Constructors: - {@code ChecksumException(String message)}: Creates an exception instance with
 * a detailed error message describing the specific checksum issue. - {@code
 * ChecksumException(String message, Throwable cause)}: Creates an exception instance with a
 * detailed error message and a throwable cause to provide additional context about the error.
 */
public class ChecksumException extends RuntimeException {
  public ChecksumException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChecksumException(String message) {
    super(message);
  }
}
