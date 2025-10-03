package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

public class ChecksumException extends RuntimeException {
  public ChecksumException(String message, Throwable cause) {
    super(message, cause);
  }
}
