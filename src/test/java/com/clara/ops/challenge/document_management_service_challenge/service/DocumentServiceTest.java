package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.repositories.DocumentRepository;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private MinioService minioService;

  @InjectMocks private DocumentService documentService; // Your service implementation

  @Test
  void testUploadDocument_success() throws Exception {
    // Prepare a dummy file for upload
    byte[] fileContent = "dummy content".getBytes();
    MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent);

    // Simulate MinIO file upload: return a string representing the object's path
    String objectName = "Matthew/test.txt";
    when(minioService.uploadFile(
            eq("Matthew"),
            eq("imgM"),
            any(InputStream.class),
            eq((long) fileContent.length),
            eq("text/plain")))
        .thenReturn(objectName);

    // Create a Document to be returned by the repository on save
    Document doc = new Document();
    doc.setId(1L);
    doc.setUsername("Matthew");
    doc.setDocumentName("imgM");
    doc.setTags(Arrays.asList("tag1", "tag3"));
    doc.setMinioPath(objectName);
    doc.setFileSize((long) fileContent.length);
    doc.setFileType("text/plain");
    doc.setCreatedAt(LocalDateTime.now());

    when(documentRepository.save(any(Document.class))).thenReturn(doc);

    // Call the service method
    Document result =
        documentService.uploadDocument("Matthew", "imgM", Arrays.asList("tag1", "tag3"), file);

    // Verify expectations
    assertNotNull(result);
    assertEquals("Matthew", result.getUsername());
    assertEquals("imgM", result.getDocumentName());
    verify(minioService)
        .uploadFile(
            eq("Matthew"),
            eq("imgM"),
            any(InputStream.class),
            eq((long) fileContent.length),
            eq("text/plain"));
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void testSearchDocuments() {
    // Prepare a dummy document and wrap it in a Page
    Document doc = new Document();
    doc.setId(1L);
    doc.setUsername("Matthew");
    doc.setDocumentName("imgM");
    doc.setTags(Arrays.asList("tag1", "tag3"));
    doc.setFileSize(12345L);
    doc.setFileType("image/png");
    doc.setCreatedAt(LocalDateTime.now());
    List<Document> documents = Arrays.asList(doc);
    Page<Document> page = new PageImpl<>(documents, PageRequest.of(0, 20), 1);

    // When searchDocuments is called, return our page
    when(documentRepository.findAll(Mockito.<Specification<Document>>any(), any(PageRequest.class)))
        .thenReturn(page);

    Page<Document> result =
        documentService.searchDocuments(
            Optional.of("Matthew"),
            Optional.empty(),
            Optional.of(Arrays.asList("tag1")),
            PageRequest.of(0, 20));

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(documentRepository)
        .findAll(Mockito.<Specification<Document>>any(), any(PageRequest.class));
  }

  @Test
  void testGetDownloadUrl_success() {
    // Prepare a dummy document with a MinIO path
    Document doc = new Document();
    doc.setId(1L);
    doc.setUsername("Matthew");
    doc.setDocumentName("imgM");
    doc.setMinioPath("Matthew/imgM.txt");

    when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
    String presignedUrl = "http://example.com/download/1";
    when(minioService.getPresignedUrl("Matthew/imgM.txt")).thenReturn(presignedUrl);

    String result = documentService.getDownloadUrl(1L);
    assertEquals(presignedUrl, result);
    verify(documentRepository).findById(1L);
    verify(minioService).getPresignedUrl("Matthew/imgM.txt");
  }

  @Test
  void testGetDownloadUrl_documentNotFound() {
    when(documentRepository.findById(1L)).thenReturn(Optional.empty());
    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              documentService.getDownloadUrl(1L);
            });
    assertTrue(exception.getMessage().contains("Document not found"));
    verify(documentRepository).findById(1L);
  }
}
