package com.clara.ops.challenge.document_management_service_challenge.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The document search filters. */
@Schema(description = "The document search filters.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchFilters {

  @JsonProperty(value = "user")
  @Schema(description = "The user who uploaded the document.")
  private String user;

  @JsonProperty("name")
  @Schema(description = "The document name.")
  private String name;

  @JsonProperty("tag")
  @Schema(description = "The document tag.")
  private String tag;
}
