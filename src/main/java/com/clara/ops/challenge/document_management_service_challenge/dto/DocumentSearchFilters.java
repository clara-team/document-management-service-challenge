package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;

/** The document search filters. */
@Schema(description = "The document search filters.")
public class DocumentSearchFilters {

  @JsonProperty("user")
  @Schema(description = "The user who uploaded the document.")
  private String user;

  @JsonProperty("name")
  @Schema(description = "The document name.")
  private String name;

  @JsonProperty("tags")
  @Schema(description = "The document tags.")
  @Valid
  private List<String> tags;
}
