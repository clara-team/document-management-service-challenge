package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentDetailResponseDto;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.PaginationMetadata;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;

import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public PaginatedDocumentSearch search(DocumentSearchFilters filters, int page, int size) {
        Pageable pageable =
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<DocumentEntity> spec = buildSpecification(filters);

        Page<DocumentEntity> resultPage = documentRepository.findAll(spec, pageable);

        return PaginatedDocumentSearch.builder()
                .metadata(buildMetadata(resultPage))
                .documents(resultPage.getContent().stream().map(this::toResponse).toList())
                .build();
    }

    private Specification<DocumentEntity> buildSpecification(DocumentSearchFilters filters) {
        Specification<DocumentEntity> spec = Specification.where(null);

        if (filters == null) {
            return spec;
        }

        if (StringUtils.hasText(filters.getUser())) {
            spec = spec.and(DocumentSpecifications.userEquals(filters.getUser()));
        }
        if (StringUtils.hasText(filters.getName())) {
            spec = spec.and(DocumentSpecifications.nameLike(filters.getName()));
        }
        if (filters.getTags() != null && !filters.getTags().isEmpty()) {
            spec = spec.and(DocumentSpecifications.hasAllTags(filters.getTags()));
        }

        return spec;
    }

    private PaginationMetadata buildMetadata(Page<DocumentEntity> page) {
        return PaginationMetadata.builder()
                .currentPage(page.getNumber() + 1)
                .itemsPerPage(page.getSize())
                .currentItems(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .build();
    }

    private DocumentDetailResponseDto toResponse(DocumentEntity entity) {
        return DocumentDetailResponseDto.builder()
                .id(entity.getId())
                .user(entity.getUserName())
                .name(entity.getName())
                .tags(entity.getTags())
                .size(entity.getFileSize())
                .type(entity.getFileType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
