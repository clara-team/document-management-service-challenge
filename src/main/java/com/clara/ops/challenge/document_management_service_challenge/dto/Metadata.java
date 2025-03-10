package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** The metadata for the pagination. */
@Schema(description = "The metadata for the pagination.")
public class Metadata {

  @JsonProperty("currentPage")
  @Schema(example = "1", description = "The current page. It starts at 0.")
  private Integer currentPage;

  @JsonProperty("itemsPerPage")
  @Schema(example = "10", description = "The number of items per page, requested by the client.")
  private Integer itemsPerPage;

  @JsonProperty("currentItems")
  @Schema(
      example = "9",
      description =
          "The number of items in the current page. It may be less than the number of items per"
              + " page, if the current page is the last one.")
  private Integer currentItems;

  @JsonProperty("totalPages")
  @Schema(
      example = "2",
      description =
          "The total number of pages. It is calculated using the total number of items and the"
              + " number of items per page.")
  private Integer totalPages;

  @JsonProperty("totalItems")
  @Schema(example = "19", description = "The total number of items.")
  private Integer totalItems;
}
