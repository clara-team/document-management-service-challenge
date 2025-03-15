package com.clara.ops.challenge.document_management_service_challenge.exceptions;

/** Exception thrown when a document is not found. */
public class DataNotFound extends IllegalArgumentException {
  public DataNotFound(String message) {
    super(message);
  }
}
