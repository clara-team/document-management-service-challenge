package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** The document download URL. */
@Schema(description = "The document download URL.")
public class DocumentDownloadUrl {

  @JsonProperty("url")
  @Schema(description = "The document download URL.")
  private String url;
}
