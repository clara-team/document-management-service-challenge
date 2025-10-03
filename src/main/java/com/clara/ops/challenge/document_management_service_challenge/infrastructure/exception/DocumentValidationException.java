package com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception;

public class DocumentValidationException extends RuntimeException {
  public DocumentValidationException(String message, String userId, String fileName) {
    super(String.format("%s (userId=%s, fileName=%s)", message, userId, fileName));
  }
}
