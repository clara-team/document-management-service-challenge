package com.clara.ops.challenge.document_management_service_challenge.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String timestamp;
    private Integer statusCode;
    private String message;
    private String path;
    private List<String> errors;

}
