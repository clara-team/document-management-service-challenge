package com.clara.ops.challenge.document_management_service_challenge.web.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.clara.ops.challenge.document_management_service_challenge.application.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DocumentNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 — Bean Validation failures (@NotBlank, @NotEmpty, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, details);
    }

    // 400 — Path variable type mismatch (e.g. non-UUID passed to documentId)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message =
                "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // 400 — File exceeds the configured max-file-size
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.BAD_REQUEST, "File size exceeds the maximum allowed limit of 500MB");
    }

    // 400 — Invalid arguments from service layer (e.g. non-PDF content type)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 503 — Storage layer failure (MinIO unreachable, etc.)
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException ex) {
        log.error("Storage error", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Storage service is unavailable. Please try again later.");
    }

    // 500 — Catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body =
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build();
        return ResponseEntity.status(status).body(body);
    }
}
