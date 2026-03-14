package com.clara.ops.challenge.document_management_service_challenge.application.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(UUID documentId) {
        super("Document not found: " + documentId);
    }
}
