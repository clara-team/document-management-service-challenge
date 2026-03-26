package com.clara.ops.challenge.document_management_service_challenge.exception;

import com.clara.ops.challenge.document_management_service_challenge.exception.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class AppControllerAdvice {

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ErrorResponse> applicationException(BusinessException e, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now().toString())
                .statusCode(e.getHttpStatus().value())
                .message(e.getMessage())
                .path(request.getServletPath())
                .build();
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now().toString())
                .statusCode(ex.getHttpStatus().value())
                .message("Required fields are empty or null.")
                .path(request.getServletPath())
                .errors(new ArrayList<String>())
                .build();

        ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .forEach(body.getErrors()::add);

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> genericException(Exception e, HttpServletRequest request) {
        if (Objects.nonNull(request)) {
            log.info("GenericException - Method: <{}>", request.getMethod());
            log.info("GenericException - ServletPath: <{}>", request.getServletPath());
        }
        log.error("GenericException - Erro Inesperado: ", e);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now().toString())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please contact our team.")
                .path(request.getServletPath())
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

}
