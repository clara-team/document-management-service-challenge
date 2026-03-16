package com.clara.ops.challenge.document_management_service_challenge.application.dto;

public record PaginationMetadata(
    int currentPage,
    int itemsPerPage,
    int currentItems,
    int totalPages,
    long totalItems) {}
