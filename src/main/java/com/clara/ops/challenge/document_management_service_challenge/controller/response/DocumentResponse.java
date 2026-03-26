package com.clara.ops.challenge.document_management_service_challenge.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The document DTO.")
public class DocumentResponse {

    @Schema(description = "The document ID.")
    private String id;

    @Schema(description = "The user who uploaded the document.")
    private String user;

    @Schema(description = "The document name.")
    private String name;

    @Schema(description = "The document tags.")
    private List<String> tags;

    @Schema(description = "The document size in bytes.")
    private BigInteger size;

    @Schema(description = "The document type.")
    private String type;

    @Schema(description = "The document creation date.")
    private String createdAt;

}
