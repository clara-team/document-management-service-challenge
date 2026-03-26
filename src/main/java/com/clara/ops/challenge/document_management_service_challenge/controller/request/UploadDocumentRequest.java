package com.clara.ops.challenge.document_management_service_challenge.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The request to upload a document.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadDocumentRequest {

    @NotBlank(message = "The user is required.")
    @Size(max = 200, message = "User must not exceed 200 characters.")
    @Schema(description = "The user who uploaded the document.", requiredMode = REQUIRED)
    private String user;

    @NotBlank(message = "The filename is required.")
    @Size(max = 200, message = "Filename must not exceed 200 characters.")
    @Schema(description = "The document name.", requiredMode = REQUIRED)
    private String name;

    @NotEmpty(message = "At least one tag is required.")
    @Schema(description = "The document tags.", requiredMode = REQUIRED)
    private Set<String> tags;

    public String getFilePath() {
        return getUser().concat("/").concat(getName());
    }
}
