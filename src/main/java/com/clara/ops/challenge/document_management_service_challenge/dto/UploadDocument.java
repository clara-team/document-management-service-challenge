package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** The request to upload a document. */
@Schema(description = "The request to upload a document.")
public class UploadDocument {

  @JsonProperty("user")
  @Schema(required = true, description = "The user who uploaded the document.")
  @NotNull private String user;

  @JsonProperty("name")
  @Schema(required = true, description = "The document name.")
  @NotNull private String name;

  @JsonProperty("tags")
  @Schema(required = true, description = "The document tags.")
  @NotNull @Valid
  private List<String> tags;
}
