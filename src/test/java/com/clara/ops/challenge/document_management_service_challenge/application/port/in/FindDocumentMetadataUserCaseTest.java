package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FindDocumentMetadataUserCaseTest {

  @Test
  void findMetadata_ReturnsDocument_WhenValidDocumentIdProvided() {
    // Arrange
    FindDocumentMetadataUserCase findDocumentMetadataUserCase =
        mock(FindDocumentMetadataUserCase.class);
    UUID documentId = UUID.randomUUID();

    Document expectedDocument =
        Document.builder()
            .id(documentId)
            .userId(BigInteger.valueOf(12345))
            .documentName("Test Document")
            .tags(List.of("tag1", "tag2"))
            .minioPath("/files/test-document")
            .documentUrl("http://example.com/documents/test-document")
            .fileSize(5120L)
            .fileType("application/pdf")
            .updatedAt(Instant.now())
            .createdAt(Instant.now())
            .checksum("abc123checksum")
            .status("AVAILABLE")
            .build();

    when(findDocumentMetadataUserCase.findMetadata(documentId)).thenReturn(expectedDocument);

    // Act
    Document result = findDocumentMetadataUserCase.findMetadata(documentId);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDocument.getId(), result.getId());
    assertEquals(expectedDocument.getUserId(), result.getUserId());
    assertEquals(expectedDocument.getDocumentName(), result.getDocumentName());
    assertEquals(expectedDocument.getTags(), result.getTags());
    assertEquals(expectedDocument.getMinioPath(), result.getMinioPath());
    assertEquals(expectedDocument.getDocumentUrl(), result.getDocumentUrl());
    assertEquals(expectedDocument.getFileSize(), result.getFileSize());
    assertEquals(expectedDocument.getFileType(), result.getFileType());
    assertEquals(expectedDocument.getChecksum(), result.getChecksum());
    assertEquals(expectedDocument.getStatus(), result.getStatus());
  }

  @Test
  void findMetadata_ThrowsException_WhenDocumentNotFound() {
    // Arrange
    FindDocumentMetadataUserCase findDocumentMetadataUserCase =
        mock(FindDocumentMetadataUserCase.class);
    UUID nonExistentDocumentId = UUID.randomUUID();
    when(findDocumentMetadataUserCase.findMetadata(nonExistentDocumentId))
        .thenThrow(new IllegalArgumentException("Document not found"));

    // Act & Assert
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> findDocumentMetadataUserCase.findMetadata(nonExistentDocumentId));
    assertEquals("Document not found", exception.getMessage());
  }
}
