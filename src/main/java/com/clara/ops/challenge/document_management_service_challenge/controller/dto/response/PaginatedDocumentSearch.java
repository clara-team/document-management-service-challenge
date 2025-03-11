package com.clara.ops.challenge.document_management_service_challenge.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The paginated document search response. */
@Schema(description = "The paginated document search response.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDocumentSearch {

  @JsonProperty("metadata")
  @Schema(description = "Any metadata associated with the document")
  private Metadata metadata;

  @JsonProperty("documents")
  @Schema(description = "The list of documents.")
  private List<Document> documents;
}
