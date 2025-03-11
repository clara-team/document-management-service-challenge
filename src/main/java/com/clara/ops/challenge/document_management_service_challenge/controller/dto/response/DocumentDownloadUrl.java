package com.clara.ops.challenge.document_management_service_challenge.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The document download URL. */
@Schema(description = "The document download URL.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDownloadUrl {

  @JsonProperty("url")
  @Schema(description = "The document download URL.")
  private String url;
}
