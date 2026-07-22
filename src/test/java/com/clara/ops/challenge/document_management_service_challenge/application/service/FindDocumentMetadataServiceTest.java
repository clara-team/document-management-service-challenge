package com.clara.ops.challenge.document_management_service_challenge.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentNotFoundException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FindDocumentMetadataServiceTest {

  @Test
  void testFindMetadataSuccessfully() {
    // Arrange
    UUID documentId = UUID.randomUUID();
    DocumentRepositoryPort documentRepositoryPort = mock(DocumentRepositoryPort.class);
    FindDocumentMetadataService service = new FindDocumentMetadataService(documentRepositoryPort);

    Document document =
        Document.builder()
            .id(documentId)
            .userId(BigInteger.ONE)
            .documentName("example.pdf")
            .tags(Collections.singletonList("tag1"))
            .minioPath("path/to/file")
            .documentUrl("http://example.com/document")
            .fileSize(1024L)
            .fileType("application/pdf")
            .updatedAt(Instant.now())
            .createdAt(Instant.now())
            .checksum("checksum123")
            .status("ACTIVE")
            .build();

    when(documentRepositoryPort.findById(documentId)).thenReturn(Optional.of(document));

    // Act
    Document result = service.findMetadata(documentId);

    // Assert
    assertEquals(document, result);
  }

  @Test
  void testFindMetadataThrowsDocumentNotFoundException() {
    // Arrange
    UUID documentId = UUID.randomUUID();
    DocumentRepositoryPort documentRepositoryPort = mock(DocumentRepositoryPort.class);
    FindDocumentMetadataService service = new FindDocumentMetadataService(documentRepositoryPort);

    when(documentRepositoryPort.findById(documentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(DocumentNotFoundException.class, () -> service.findMetadata(documentId));
  }
}
