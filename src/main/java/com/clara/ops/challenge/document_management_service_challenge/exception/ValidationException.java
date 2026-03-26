package com.clara.ops.challenge.document_management_service_challenge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class ValidationException extends RuntimeException{

    @Getter
    private final BindingResult bindingResult;
    @Getter
    private final HttpStatus httpStatus;

    public ValidationException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        this.httpStatus = BAD_REQUEST;
    }

}
