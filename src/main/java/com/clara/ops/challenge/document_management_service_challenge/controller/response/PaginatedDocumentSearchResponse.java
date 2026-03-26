package com.clara.ops.challenge.document_management_service_challenge.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The paginated document search response.")
public class PaginatedDocumentSearchResponse {

    private MetadataResponse metadata;

    @Schema(description = "The list of documents.")
    private List<DocumentResponse> document;
}
