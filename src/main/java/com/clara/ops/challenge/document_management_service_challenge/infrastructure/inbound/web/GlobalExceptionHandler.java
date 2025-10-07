package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Global exception handler providing centralized exception handling across the application.
 *
 * <p>This class uses Spring's {@code @RestControllerAdvice} annotation to intercept and manage
 * exceptions thrown by controller classes. It ensures consistent and user-friendly error responses
 * for both expected and unexpected application errors.
 *
 * <p>Exception Categories: - Upload/Validation Exceptions: Handles exceptions related to file
 * upload, validation errors, missing request parts, and other validation-related issues. -
 * Download/Retrieval Exceptions: Handles exceptions encountered during data retrieval or download
 * operations. - Fallback Handlers: Provides fallback mechanisms for uncaught exceptions and generic
 * cases.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // UPLOAD / VALIDATION EXCEPTIONS -
  /**
   * Handles MaxUploadSizeExceededException by returning a ResponseEntity with a PAYLOAD_TOO_LARGE
   * error response.
   *
   * @param ex the exception thrown when the uploaded file exceeds the maximum allowed size
   * @return a ResponseEntity containing an ErrorResponse with details about the PAYLOAD_TOO_LARGE
   *     error
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(
            new ErrorResponse(
                ErrorCode.PAYLOAD_TOO_LARGE.name(),
                ErrorCode.PAYLOAD_TOO_LARGE.getDefaultMessage()));
  }

  @ExceptionHandler(DocumentValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(DocumentValidationException ex) {
    String message = ex.getMessage();

    HttpStatus status;
    if (message.startsWith("Document already exists")) {
      status = HttpStatus.CONFLICT; // 409
    } else if (message.startsWith("Document not found")) {
      status = HttpStatus.NOT_FOUND; // 404
    } else {
      status = HttpStatus.BAD_REQUEST; // fallback
    }

    Map<String, Object> body = new HashMap<>();
    body.put("code", status.name());
    body.put("message", message);
    body.put("timestamp", Instant.now().toString());

    return ResponseEntity.status(status).body(body);
  }

  /**
   * Handles exceptions of type ChecksumException and returns a standardized error response.
   *
   * @param ex the ChecksumException being handled
   * @return a ResponseEntity containing an ErrorResponse with error details and HTTP status
   */
  @ExceptionHandler(ChecksumException.class)
  public ResponseEntity<ErrorResponse> handleChecksum(ChecksumException ex) {
    return ResponseEntity.status(ErrorCode.CHECKSUM_MISMATCH.getStatus())
        .body(
            new ErrorResponse(
                ErrorCode.CHECKSUM_MISMATCH.name(),
                ErrorCode.CHECKSUM_MISMATCH.getDefaultMessage()));
  }

  /**
   * Handles the case where a required part of a multipart request is missing and returns a
   * structured error response with appropriate details.
   *
   * @param ex the exception thrown when a required request part is missing
   * @return a {@code ResponseEntity} containing an {@code ErrorResponse} object with error code and
   *     a detailed error message indicating the missing part
   */
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestPart(
      MissingServletRequestPartException ex) {
    String message =
        String.format(ErrorCode.MISSING_REQUEST_PART.getDefaultMessage(), ex.getRequestPartName());
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.MISSING_REQUEST_PART.name(), message));
  }

  // DOWNLOAD / RETRIEVAL EXCEPTIONS
  /**
   * Handles the {@link DocumentNotFoundException} and returns an appropriate error response.
   *
   * @param ex the {@link DocumentNotFoundException} thrown when a requested document is not found
   * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with HTTP status 404
   *     (NOT_FOUND) and error details including the error code and message
   */
  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
    log.warn("Document not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ErrorCode.DOCUMENT_NOT_FOUND.name(), ex.getMessage()));
  }

  /**
   * Handles the occurrence of a {@link DownloadDocumentException}. Logs the error and returns an
   * appropriate response with the HTTP status code {@code 502 Bad Gateway}.
   *
   * @param ex the instance of {@code DownloadDocumentException} describing the failure during
   *     document download.
   * @return a {@code ResponseEntity} containing an {@code ErrorResponse} with details about the
   *     error.
   */
  @ExceptionHandler(DownloadDocumentException.class)
  public ResponseEntity<ErrorResponse> handleDownloadFailure(DownloadDocumentException ex) {
    log.error("Download error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse(ErrorCode.DOWNLOAD_ERROR.name(), ex.getMessage()));
  }

  /**
   * Handles uncaught runtime exceptions in the application. Captures the details of the exception,
   * logs it, and returns a standardized error response to the client.
   *
   * @param ex the runtime exception that was thrown during request processing
   * @return a {@code ResponseEntity}
   */
  // FALLBACK HANDLERS
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
    log.error("Unexpected runtime error: {}", ex.getMessage(), ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage());
  }

  /**
   * Handles generic exceptions not explicitly mapped to other handlers.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an {@link ErrorResponse} with an error code of
   *     INTERNAL_ERROR and the exception message, along with an HTTP status of
   *     INTERNAL_SERVER_ERROR
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), ex.getMessage()));
  }

  /**
   * Constructs a structured HTTP response containing a status, code, message, and a timestamp.
   *
   * @param status the HTTP status to be set in the response
   * @param code the code indicating the type or category of the response
   * @param message the message providing additional details about the response
   * @return a ResponseEntity containing a Map with
   */
  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String code, String message) {
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "code", code,
                "message", message,
                "timestamp", Instant.now().toString()));
  }
}
