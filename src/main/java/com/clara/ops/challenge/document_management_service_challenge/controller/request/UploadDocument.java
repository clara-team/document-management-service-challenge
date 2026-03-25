package com.clara.ops.challenge.document_management_service_challenge.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The request to upload a document.")
public class UploadDocument {

    @Schema(description = "The user who uploaded the document.")
    private String user;

    @Schema(description = "The document name.")
    private String name;

    @Schema(description = "The document tags.")
    private Set<String> tags;
}
