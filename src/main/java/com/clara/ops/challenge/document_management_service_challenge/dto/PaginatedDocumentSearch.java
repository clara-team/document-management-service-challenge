package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;

/** The paginated document search response. */
@Schema(description = "The paginated document search response.")
public class PaginatedDocumentSearch {

  @JsonProperty("metadata")
  @Schema(description = "Any metadata associated with the document")
  private Metadata metadata;

  @JsonProperty("documents")
  @Schema(description = "The list of documents.")
  @Valid
  private List<Document> documents;
}
