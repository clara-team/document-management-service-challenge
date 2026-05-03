package com.clara.ops.challenge.document_management_service_challenge.exception;

public class UploadLimitException extends RuntimeException {
  public UploadLimitException(final String message) {
    super(message);
  }
}
