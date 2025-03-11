package com.clara.ops.challenge.document_management_service_challenge.exception;

public class DocumentNotFoundException extends RuntimeException {

  public DocumentNotFoundException(String message) {
    super(message);
  }

  public DocumentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
