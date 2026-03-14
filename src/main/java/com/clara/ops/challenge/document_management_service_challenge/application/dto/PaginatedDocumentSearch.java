package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PaginatedDocumentSearch {

    private PaginationMetadata metadata;
    private List<DocumentDetailResponseDto> documents;
}
