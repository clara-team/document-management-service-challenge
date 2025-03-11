package com.clara.ops.challenge.document_management_service_challenge.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The request to upload a document. */
@Schema(description = "The request to upload a document.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocument {

  @JsonProperty("user")
  @Schema(required = true, description = "The user who uploaded the document.")
  private String user;

  @JsonProperty("name")
  @Schema(required = true, description = "The document name.")
  private String name;

  @JsonProperty("tags")
  @Schema(required = true, description = "The document tags.")
  private List<String> tags;
}
