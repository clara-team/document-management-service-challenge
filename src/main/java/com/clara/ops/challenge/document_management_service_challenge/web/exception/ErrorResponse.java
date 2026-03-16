package com.clara.ops.challenge.document_management_service_challenge.web.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}
