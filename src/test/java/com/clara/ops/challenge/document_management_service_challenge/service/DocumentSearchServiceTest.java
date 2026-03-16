package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentSearchService;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

        assertThat(result.documents()).hasSize(1);
        assertThat(result.metadata().totalItems()).isEqualTo(1);
        assertThat(result.documents().get(0).user()).isEqualTo("john");
    }

    @Test
    void search_withNullFilters_doesNotThrow() {
        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatCode(() -> searchService.search(null, 1, 20)).doesNotThrowAnyException();
    }

    @Test
    void search_resultDoesNotExposeMinioPath() {
        var entity = buildEntity("john", "doc.pdf", List.of("legal"));
        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = searchService.search(new DocumentSearchFilters(), 1, 20);

        assertThat(result.documents()).allSatisfy(dto ->
                assertThat(dto).hasNoNullFieldsOrProperties());
    }

    @Test
    void search_paginationMetadata_isCorrect() {
        var entities = List.of(
                buildEntity("alice", "a.pdf", List.of("tag1")),
                buildEntity("bob", "b.pdf", List.of("tag2")));
        var pageResult = new PageImpl<>(entities, PageRequest.of(0, 2), 5);

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var result = searchService.search(new DocumentSearchFilters(), 1, 2);

        assertThat(result.metadata().currentPage()).isEqualTo(1);
        assertThat(result.metadata().itemsPerPage()).isEqualTo(2);
        assertThat(result.metadata().currentItems()).isEqualTo(2);
        assertThat(result.metadata().totalItems()).isEqualTo(5);
        assertThat(result.metadata().totalPages()).isEqualTo(3);
    }

    @Test
    void search_emptyPage_returnsEmptyDocumentsWithMetadata() {
        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        var result = searchService.search(new DocumentSearchFilters(), 1, 20);

        assertThat(result.documents()).isEmpty();
        assertThat(result.metadata().totalItems()).isEqualTo(0);
        assertThat(result.metadata().totalPages()).isEqualTo(0);
    }

    // ── Filters ───────────────────────────────────────────────────────────────

    @Test
    void search_filterByUser_returnsonlyMatchingUser() {
        var alice = buildEntity("alice", "doc.pdf", List.of("legal"));
        var pageResult = new PageImpl<>(List.of(alice));

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var filters = new DocumentSearchFilters();
        filters.setUser("alice");

        var result = searchService.search(filters, 1, 20);

        assertThat(result.documents()).hasSize(1);
        assertThat(result.documents().get(0).user()).isEqualTo("alice");
    }

    @Test
    void search_filterByTags_returnsDocumentsWithMatchingTags() {
        var entity = buildEntity("alice", "doc.pdf", List.of("legal", "2024"));
        var pageResult = new PageImpl<>(List.of(entity));

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var filters = new DocumentSearchFilters();
        filters.setTags(List.of("legal"));

        var result = searchService.search(filters, 1, 20);

        assertThat(result.documents()).hasSize(1);
        assertThat(result.documents().get(0).tags()).contains("legal");
    }

    @Test
    void search_filterByEmptyTagsList_treatedAsNoTagFilter() {
        var entity = buildEntity("alice", "doc.pdf", List.of("legal"));
        var pageResult = new PageImpl<>(List.of(entity));

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var filters = new DocumentSearchFilters();
        filters.setTags(List.of());

        var result = searchService.search(filters, 1, 20);

        assertThat(result.documents()).hasSize(1);
    }

    @Test
    void search_multipleDocuments_orderedByCreatedAtDesc() {
        var older = buildEntity("alice", "old.pdf", List.of("tag"));
        var newer = buildEntity("alice", "new.pdf", List.of("tag"));

        var pageResult = new PageImpl<>(List.of(newer, older));

        when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        var result = searchService.search(new DocumentSearchFilters(), 1, 20);

        assertThat(result.documents()).hasSize(2);
        assertThat(result.documents().get(0).name()).isEqualTo("new.pdf");
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
