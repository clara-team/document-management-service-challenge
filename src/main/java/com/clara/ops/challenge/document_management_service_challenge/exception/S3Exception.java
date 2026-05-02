package com.clara.ops.challenge.document_management_service_challenge.exception;

public class S3Exception extends RuntimeException {
    public S3Exception(final String message) {
        super(message);
    }
}
