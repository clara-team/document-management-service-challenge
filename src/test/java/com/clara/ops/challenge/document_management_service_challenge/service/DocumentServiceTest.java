package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import io.minio.MinioClient;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

class DocumentServiceTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private UserRepository userRepository;

  @Mock private MinioClient minioClient;

  @InjectMocks private DocumentService documentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(documentService, "bucketName", "cucket-name");
  }

  @Test
  void testGetDocuments() {
    // Setup
    var user = User.builder().id(1L).username("bobsmith").build();

    var document =
        Document.builder()
            .id(1L)
            .name("Test Document")
            .path("test/path")
            .size(1024L)
            .tags(Arrays.asList("tag1", "tag2"))
            .user(user)
            .createdAt(LocalDateTime.now())
            .build();

    var documentPage = new PageImpl<>(List.of(document), PageRequest.of(0, 10), 1);
    when(userRepository.findByUsername("bobsmith")).thenReturn(Optional.of(user));
    when(documentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(documentPage);

    // Execute
    var result = documentService.getDocuments("bobsmith", null, null, 1, 10);

    // Verify
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Test Document", result.getContent().get(0).name());
  }

  @Test
  void testGenerateDownloadUrl() throws Exception {
    // Setup
    var document = Document.builder().id(1L).path("test/path").build();

    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://example.com/download");

    // Execute
    var downloadUrl = documentService.generateDownloadUrl(1L);

    // Verify
    assertEquals("http://example.com/download", downloadUrl);
  }

  @Test
  void testUploadFile() throws Exception {
    // Setup
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test-file.pdf");
    when(file.getContentType()).thenReturn("application/pdf");
    when(file.getSize()).thenReturn(1024L);

    var user = User.builder().id(1L).username("bobsmith").build();

    when(userRepository.findByUsername("bobsmith")).thenReturn(Optional.of(user));
    when(documentRepository.save(any(Document.class)))
        .thenReturn(Document.builder().id(1L).build());

    HashMap<String, String> metadata = new HashMap<String, String>();
    metadata.put("username", "bobsmith");
    metadata.put("tags", "tag1,tag2");

    // Execute
    documentService.uploadFile(file, metadata);

    TimeUnit.SECONDS.sleep(1);

    // Verify
    verify(documentRepository, times(1)).save(any());
  }
}
