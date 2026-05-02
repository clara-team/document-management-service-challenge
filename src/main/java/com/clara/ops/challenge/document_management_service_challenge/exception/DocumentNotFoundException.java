package com.clara.ops.challenge.document_management_service_challenge.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(final String message) {
        super(message);
    }
}
