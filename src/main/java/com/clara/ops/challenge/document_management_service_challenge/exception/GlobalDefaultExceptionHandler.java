package com.clara.ops.challenge.document_management_service_challenge.exception;

import io.minio.errors.InternalException;
import io.minio.errors.MinioException;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleBadRequest(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<Object> handleUnauthorized(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.UNAUTHORIZED,
        request);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<Object> handleForbidden(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.FORBIDDEN,
        request);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<Object> handleNotFound(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request);
  }

  @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<Object> handleConflict(RuntimeException exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.CONFLICT,
        request);
  }

  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  public ResponseEntity<Object> handlePayloadTooLarge(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .error("Payload Too Large")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.PAYLOAD_TOO_LARGE,
        request);
  }

  @ExceptionHandler({InternalException.class, MinioException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleInternalServerError(Exception exception, WebRequest request) {
    return handleExceptionInternal(
        exception,
        ErrorExceptionResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message(exception.getMessage())
            .build(),
        new HttpHeaders(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }
}
