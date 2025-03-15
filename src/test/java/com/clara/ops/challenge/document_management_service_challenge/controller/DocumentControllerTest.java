package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.dto.DownloadUrlDto;
import com.clara.ops.challenge.document_management_service_challenge.exceptions.DataNotFound;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.minio.errors.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

public class DocumentControllerTest {

  @InjectMocks private DocumentController documentController;

  @Mock private DocumentService documentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetDocuments_Success() {
    // Given
    String user = "bobsmith";
    String documentName = "report.pdf";
    String tags = "confidential";
    int page = 1;
    int size = 10;
    var documentPage = mock(Page.class);

    when(documentService.getDocuments(user, documentName, tags, page, size))
        .thenReturn(documentPage);

    // When
    var response = documentController.getDocuments(user, documentName, tags, page, size);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(documentPage, response.getBody());
    verify(documentService, times(1)).getDocuments(user, documentName, tags, page, size);
  }

  @Test
  void testDownloadDocument_Success()
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
    // Given
    var documentId = 1L;
    var downloadUrl = "http://example.com/download";
    var downloadUrlDto = new DownloadUrlDto(downloadUrl);

    when(documentService.generateDownloadUrl(documentId)).thenReturn(downloadUrlDto);

    // When
    var response = documentController.downloadDocument(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(downloadUrl, response.getBody().url());
  }

  @Test
  void testDownloadDocument_NotFound()
      throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
    // Given
    var documentId = 1L;
    when(documentService.generateDownloadUrl(documentId))
        .thenThrow(new DataNotFound("Document not found"));

    // When
    var exception =
        assertThrows(
            DataNotFound.class,
            () -> {
              documentController.downloadDocument(documentId);
            });

    // Then
    assertEquals("Document not found", exception.getMessage());
  }

  @Test
  void testCreateDocument_Success()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InterruptedException,
          InternalException {
    // Given
    var file =
        new MockMultipartFile(
            "file", "test-file.pdf", "application/pdf", "test content".getBytes());
    Map<String, String> metadata = new HashMap<>();
    metadata.put("username", "bobsmith");

    // When
    var response = documentController.createDocument(file, metadata);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("File uploaded successfully", response.getBody());
    verify(documentService, times(1)).uploadFile(file, metadata);
  }

  @Test
  void testCreateDocument_FileTooLarge()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InterruptedException,
          InternalException {
    // Given
    var file =
        new MockMultipartFile(
            "file", "large-file.pdf", "application/pdf", new byte[600 * 1024 * 1024]); // 600MB
    Map<String, String> metadata = new HashMap<>();

    // When
    ResponseEntity<String> response = documentController.createDocument(file, metadata);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("File size exceeds the maximum limit of 500MB.", response.getBody());
    verify(documentService, never()).uploadFile(file, metadata);
  }
}
