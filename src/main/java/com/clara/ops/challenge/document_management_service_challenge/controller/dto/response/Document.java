package com.clara.ops.challenge.document_management_service_challenge.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The document DTO. */
@Schema(description = "The document DTO.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

  @JsonProperty("id")
  @Schema(description = "The document ID.")
  private String id;

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

  @JsonProperty("size")
  @Schema(description = "The document size in bytes.")
  private Integer size;

  @JsonProperty("type")
  @Schema(description = "The document type.")
  private String type;

  @JsonProperty("createdAt")
  @Schema(description = "The document creation date.")
  private String createdAt;
}
