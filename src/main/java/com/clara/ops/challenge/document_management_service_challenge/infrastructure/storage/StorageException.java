package com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage;

public class StorageException extends RuntimeException  {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
