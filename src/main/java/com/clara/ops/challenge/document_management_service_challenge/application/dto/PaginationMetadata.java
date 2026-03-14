package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationMetadata {
    private int currentPage;
    private int itemsPerPage;
    private int currentItems;
    private int totalPages;
    private long totalItems;

}
