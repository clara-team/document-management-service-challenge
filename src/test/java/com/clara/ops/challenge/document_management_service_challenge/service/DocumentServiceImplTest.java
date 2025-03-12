package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.model.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.entity.Tag;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.impl.DocumentServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private TagRepository tagRepository;

  @Mock private StorageService storageService;

  @InjectMocks private DocumentServiceImpl documentService;

  private Document testDocument;
  private MultipartFile testFile;
  private Tag testTag1;
  private Tag testTag2;
  private UploadRequest uploadRequest;
  private SearchRequest searchRequest;

  @BeforeEach
  void setUp() {
    // Set up tags
    testTag1 = new Tag();
    testTag1.setId(1L);
    testTag1.setName("tag1");
    testTag1.setDocuments(new HashSet<>());

    testTag2 = new Tag();
    testTag2.setId(2L);
    testTag2.setName("tag2");
    testTag2.setDocuments(new HashSet<>());

    Set<Tag> tags = new HashSet<>(Arrays.asList(testTag1, testTag2));

    // Set up test document
    testDocument =
        Document.builder()
            .id(1L)
            .userId("test-user")
            .documentName("test-document.pdf")
            .minioPath("test-user/test-document.pdf")
            .fileSize(1024L)
            .fileType("application/pdf")
            .createdAt(LocalDateTime.now())
            .tags(tags)
            .build();

    // Set up test file
    testFile =
        new MockMultipartFile(
            "file", "test-document.pdf", "application/pdf", "Test PDF content".getBytes());

    // Set up upload request
    uploadRequest =
        UploadRequest.builder()
            .userId("test-user")
            .documentName("test-document.pdf")
            .tags(Arrays.asList("tag1", "tag2"))
            .build();

    // Set up search request
    searchRequest =
        SearchRequest.builder()
            .userId("test-user")
            .documentName("test")
            .tags(Arrays.asList("tag1"))
            .page(0)
            .size(10)
            .build();
  }

  @Test
  void uploadDocument_Success() {
    // Setup mock behavior
    when(storageService.uploadFile(anyString(), anyString(), any(MultipartFile.class)))
        .thenReturn("test-user/test-document.pdf");
    when(tagRepository.findByName("tag1")).thenReturn(Optional.of(testTag1));
    when(tagRepository.findByName("tag2")).thenReturn(Optional.of(testTag2));
    when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

    // Call service method
    DocumentInfo result = documentService.uploadDocument(uploadRequest, testFile);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUserId()).isEqualTo("test-user");
    assertThat(result.getDocumentName()).isEqualTo("test-document.pdf");
    assertThat(result.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
    assertThat(result.getFileSize()).isEqualTo(1024L);

    // Verify interactions
    verify(storageService)
        .uploadFile(eq("test-user"), eq("test-document.pdf"), any(MultipartFile.class));
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void uploadDocument_NewTags() {
    // Setup mock behavior
    when(storageService.uploadFile(anyString(), anyString(), any(MultipartFile.class)))
        .thenReturn("test-user/test-document.pdf");
    when(tagRepository.findByName("tag1")).thenReturn(Optional.empty()); // Tag doesn't exist yet
    when(tagRepository.findByName("tag2")).thenReturn(Optional.empty());
    when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

    // Call service method
    DocumentInfo result = documentService.uploadDocument(uploadRequest, testFile);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);

    // Verify interactions
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void searchDocuments_AllFilters() {
    // Setup mock behavior
    Page<Document> documentPage =
        new PageImpl<>(Collections.singletonList(testDocument), PageRequest.of(0, 10), 1);

    when(documentRepository.findByUserIdAndDocumentNameAndTagNamesExactMatch(
            anyString(), anyString(), any(List.class), anyLong(), any(Pageable.class)))
        .thenReturn(documentPage);

    // Call service method
    SearchResponse result = documentService.searchDocuments(searchRequest);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDocuments()).hasSize(1);
    assertThat(result.getCurrentPage()).isEqualTo(0);
    assertThat(result.getTotalItems()).isEqualTo(1);
    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getDocuments().get(0).getId()).isEqualTo(1L);
  }

  @Test
  void searchDocuments_OnlyUserId() {
    // Setup mock behavior
    Page<Document> documentPage =
        new PageImpl<>(Collections.singletonList(testDocument), PageRequest.of(0, 10), 1);

    SearchRequest request = SearchRequest.builder().userId("test-user").page(0).size(10).build();

    when(documentRepository.findByUserIdOrderByCreatedAtDesc(anyString(), any(Pageable.class)))
        .thenReturn(documentPage);

    // Call service method
    SearchResponse result = documentService.searchDocuments(request);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDocuments()).hasSize(1);
  }

  @Test
  void searchDocuments_OnlyDocumentName() {
    // Setup mock behavior
    Page<Document> documentPage =
        new PageImpl<>(Collections.singletonList(testDocument), PageRequest.of(0, 10), 1);

    SearchRequest request = SearchRequest.builder().documentName("test").page(0).size(10).build();

    when(documentRepository.findByDocumentNameContainingIgnoreCaseOrderByCreatedAtDesc(
            anyString(), any(Pageable.class)))
        .thenReturn(documentPage);

    // Call service method
    SearchResponse result = documentService.searchDocuments(request);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDocuments()).hasSize(1);
  }

  @Test
  void searchDocuments_OnlyTags() {
    // Setup mock behavior
    Page<Document> documentPage =
        new PageImpl<>(Collections.singletonList(testDocument), PageRequest.of(0, 10), 1);

    SearchRequest request =
        SearchRequest.builder().tags(Arrays.asList("tag1")).page(0).size(10).build();

    when(documentRepository.findByTagNamesExactMatch(
            any(List.class), anyLong(), any(Pageable.class)))
        .thenReturn(documentPage);

    // Call service method
    SearchResponse result = documentService.searchDocuments(request);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDocuments()).hasSize(1);
  }

  @Test
  void searchDocuments_NoFilters() {
    // Setup mock behavior
    Page<Document> documentPage =
        new PageImpl<>(Collections.singletonList(testDocument), PageRequest.of(0, 10), 1);

    SearchRequest request = SearchRequest.builder().page(0).size(10).build();

    when(documentRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
        .thenReturn(documentPage);

    // Call service method
    SearchResponse result = documentService.searchDocuments(request);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDocuments()).hasSize(1);
  }

  @Test
  void getDocumentDownloadUrl_Success() {
    // Setup mock behavior
    when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
    when(storageService.generatePresignedUrl("test-user/test-document.pdf"))
        .thenReturn("https://minio-endpoint/test-bucket/test-user/test-document.pdf?token=xyz");

    // Call service method
    DownloadResponse result = documentService.getDocumentDownloadUrl(1L);

    // Verify results
    assertThat(result).isNotNull();
    assertThat(result.getDownloadUrl())
        .isEqualTo("https://minio-endpoint/test-bucket/test-user/test-document.pdf?token=xyz");
    assertThat(result.getExpirationTime()).isNotNull();
  }

  @Test
  void getDocumentDownloadUrl_DocumentNotFound() {
    // Setup mock behavior
    when(documentRepository.findById(999L)).thenReturn(Optional.empty());

    // Call service method and verify exception
    assertThatThrownBy(() -> documentService.getDocumentDownloadUrl(999L))
        .isInstanceOf(DocumentNotFoundException.class)
        .hasMessageContaining("Document not found with ID: 999");
  }
}
