package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import java.util.List;

public record PaginatedDocumentSearch(
    PaginationMetadata metadata,
    List<DocumentDetailResponseDto> documents) {}
