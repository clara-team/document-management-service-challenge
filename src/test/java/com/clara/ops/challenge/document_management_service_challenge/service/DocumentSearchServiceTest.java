package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentDetailResponseDto;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentSearchService;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSearchServiceTest {

    @Mock
    DocumentRepository documentRepository;
    @InjectMocks
    DocumentSearchService searchService;

    @Test
    void search_noFilters_returnsAllDocuments() {
        var entity = buildEntity("john", "doc.pdf", List.of("legal"));
        var pageResult = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var result = searchService.search(new DocumentSearchFilters(), 1, 20);

        assertThat(result.getDocuments()).hasSize(1);
        assertThat(result.getMetadata().getTotalItems()).isEqualTo(1);
        assertThat(result.getDocuments().get(0).getUser()).isEqualTo("john");
    }

    @Test
    void search_withNullFilters_doesNotThrow() {
        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatCode(() -> searchService.search(null, 1, 20)).doesNotThrowAnyException();
    }

    @Test
    void search_resultDoesNotExposeMinioPath() {
        var entity = buildEntity("john", "doc.pdf", List.of());
        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = searchService.search(new DocumentSearchFilters(), 1, 20);

        assertThat(result.getDocuments().get(0))
                .hasNoNullFieldsOrPropertiesExcept()
                .isInstanceOf(DocumentDetailResponseDto.class);
    }

    private DocumentEntity buildEntity(String user, String name, List<String> tags) {
        return DocumentEntity.builder()
                .id(UUID.randomUUID())
                .userName(user)
                .name(name)
                .minioPath(user + "/" + name)
                .fileSize(1024L)
                .fileType("application/pdf")
                .tags(tags)
                .createdAt(LocalDateTime.now())
                .build();
    }
}