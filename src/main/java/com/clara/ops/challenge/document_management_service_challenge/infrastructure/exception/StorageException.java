package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

/**
 * A custom exception that extends {@link RuntimeException} and is used to represent storage-related
 * errors in the application. This exception can be thrown when an unexpected issue occurs during
 * storage operations such as file uploads or downloads.
 *
 * <p>The {@code StorageException} allows for specifying a custom error message and an optional
 * {@code Throwable} cause to provide additional context about the error.
 *
 * <p>Typical usage includes situations where issues with underlying storage systems are
 * encountered, which need to trigger a higher-level application error.
 *
 * <p>Constructors: - {@code StorageException(String message, Throwable cause)}: Creates an
 * exception with a detailed error message and an optional cause for the exception.
 */
public class StorageException extends RuntimeException {
  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
