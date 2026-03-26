package com.clara.ops.challenge.document_management_service_challenge.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The metadata for the pagination.")
public class MetadataResponse {

    @Schema(description = "The current page. It starts at 0.")
    private Integer currentPage;

    @Schema(description = "The number of items per page, requested by the client.")
    private Integer itemsPerPage;

    @Schema(description = "The number of items in the current page. " +
            "It may be less than the number of items per page, if the current page is the last one.")
    private Integer currentItems;

    @Schema(description = "The total number of pages. It is calculated using the total number of items " +
            "and the number of items per page.")
    private Integer totalPages;

    @Schema(description = "The total number of items.")
    private Integer totalItems;

}
