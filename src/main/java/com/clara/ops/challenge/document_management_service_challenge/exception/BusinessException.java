package com.clara.ops.challenge.document_management_service_challenge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BusinessException extends RuntimeException {

    @Getter
    private final HttpStatus httpStatus;

    public BusinessException(String messageKey) {
        super(messageKey);
        this.httpStatus = BAD_REQUEST;
    }

}
