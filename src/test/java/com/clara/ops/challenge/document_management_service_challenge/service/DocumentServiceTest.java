package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.request.DocumentSearchFilter;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.Document;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.UploadLimitException;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private FileUploadService fileUploadService;

  @Mock private DocumentRepository documentRepository;

  @InjectMocks private DocumentService documentService;

  @Test
  @DisplayName("upload: builds and formats objectKey in lowercase and trimmed")
  void uploadBuildsObjectKeyFromUserIdAndDocumentName() {
    Document document = Document.builder().userId("  User1  ").documentName("  MyDoc  ").build();
    MultipartFile file = mock(MultipartFile.class);

    documentService.upload(document, file);

    verify(fileUploadService).uploadFile(file, "user1/mydoc.pdf");
    assertThat(document.getFilePath()).isEqualTo("user1/mydoc.pdf");
    verify(documentRepository).save(document);
  }

  @Test
  @DisplayName("upload: throws IllegalStateException when 10 semaphore permits are taken")
  void uploadThrowsWhenCapacityIsReached() throws Exception {
    Semaphore semaphore = getUploadSemaphore();
    semaphore.tryAcquire(10);
    try {
      assertThatThrownBy(
              () -> documentService.upload(mock(Document.class), mock(MultipartFile.class)))
          .isInstanceOf(UploadLimitException.class)
          .hasMessageContaining("Upload capacity reached");
    } finally {
      semaphore.release(10);
    }
  }

  @Test
  @DisplayName("upload: releases semaphore permit even when an exception is thrown")
  void uploadReleasesSemaphorePermitOnException() throws Exception {
    Document document = Document.builder().userId("user").documentName("doc").build();
    MultipartFile file = mock(MultipartFile.class);
    doThrow(new RuntimeException("storage error")).when(fileUploadService).uploadFile(any(), any());

    Semaphore semaphore = getUploadSemaphore();
    int permitsBefore = semaphore.availablePermits();

    assertThatThrownBy(() -> documentService.upload(document, file))
        .isInstanceOf(RuntimeException.class);

    assertThat(semaphore.availablePermits()).isEqualTo(permitsBefore);
  }

  @Test
  @DisplayName("getDownloadUrlById: returns DocumentResponse with id, name and presigned URL")
  void getDownloadUrlByIdReturnsDocumentResponse() {
    Document document =
        Document.builder().id(1L).documentName("report").filePath("user/report.pdf").build();
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(fileUploadService.getDownloadLink("user/report.pdf"))
        .thenReturn("http://miniourl/user/report.pdf");

    DocumentResponse response = documentService.getDownloadUrlById(1L);

    assertThat(response.documentId()).isEqualTo(1L);
    assertThat(response.documentName()).isEqualTo("report");
    assertThat(response.url()).isEqualTo("http://miniourl/user/report.pdf");
  }

  @Test
  @DisplayName("getDownloadUrlById: throws DocumentNotFoundException when id does not exist")
  void getDownloadUrlByIdThrowsWhenDocumentNotFound() {
    when(documentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentService.getDownloadUrlById(99L))
        .isInstanceOf(DocumentNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  @DisplayName("filter: returns the Page returned by the repository")
  @SuppressWarnings("unchecked")
  void filterReturnsRepositoryPage() {
    DocumentSearchFilter filter = new DocumentSearchFilter("ren", "gill", Set.of("music"));
    PageRequest pageRequest = PageRequest.of(0, 5);
    Page<Document> expected =
        new PageImpl<>(List.of(Document.builder().userId("ren").documentName("gill").build()));
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(expected);

    Page<Document> result = documentService.filter(filter, pageRequest);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("filter: returns empty page when repository has no matching documents")
  @SuppressWarnings("unchecked")
  void filterReturnsEmptyPage() {
    DocumentSearchFilter filter = new DocumentSearchFilter(null, null, null);
    PageRequest pageRequest = PageRequest.of(0, 10);
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    Page<Document> result = documentService.filter(filter, pageRequest);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  @Test
  @DisplayName("filter: result page content is in createdAt descending order")
  @SuppressWarnings("unchecked")
  void filterAppliesCreatedAtDescSortAndPreservesOrder() {
    LocalDateTime older = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime newer = LocalDateTime.of(2025, 6, 1, 0, 0);
    Document newerDoc = Document.builder().documentName("newer").createdAt(newer).build();
    Document olderDoc = Document.builder().documentName("older").createdAt(older).build();
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(newerDoc, olderDoc)));

    Page<Document> result =
        documentService.filter(new DocumentSearchFilter(null, null, null), PageRequest.of(0, 10));

    assertThat(result.getContent())
        .extracting(Document::getCreatedAt)
        .containsExactly(newer, older);
  }

  @Test
  @DisplayName("filter: filters by name only")
  @SuppressWarnings("unchecked")
  void filterByNameOnly() {
    DocumentSearchFilter filter = new DocumentSearchFilter(null, "invoice", null);
    Document doc = Document.builder().userId("alice").documentName("invoice").build();
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(doc)));

    Page<Document> result = documentService.filter(filter, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getDocumentName()).isEqualTo("invoice");
  }

  @Test
  @DisplayName("filter: filters by user only")
  @SuppressWarnings("unchecked")
  void filterByUserOnly() {
    DocumentSearchFilter filter = new DocumentSearchFilter("alice", null, null);
    Document doc = Document.builder().userId("alice").documentName("legal").build();
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(doc)));

    Page<Document> result = documentService.filter(filter, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUserId()).isEqualTo("alice");
  }

  @Test
  @DisplayName("filter: filters by tags only")
  @SuppressWarnings("unchecked")
  void filterByTagsOnly() {
    DocumentSearchFilter filter = new DocumentSearchFilter(null, null, Set.of("report", "2026"));
    Document doc =
        Document.builder()
            .userId("bob")
            .documentName("test")
            .tags(List.of("report", "2026"))
            .build();
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(doc)));

    Page<Document> result = documentService.filter(filter, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTags()).containsExactlyInAnyOrder("report", "2026");
  }

  private static Semaphore getUploadSemaphore() throws Exception {
    Field field = DocumentService.class.getDeclaredField("UPLOAD_SEMAPHORE");
    field.setAccessible(true);
    return (Semaphore) field.get(null);
  }
}
