package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ApplicationException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ErrorCode;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentMapper;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence.DocumentRepository;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;

class DocumentRepositoryAdapterTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private DocumentMapper documentMapper;

  @InjectMocks private DocumentRepositoryAdapter documentRepositoryAdapter;

  public DocumentRepositoryAdapterTest() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Should save document successfully")
  void whenSaveValidDocument_thenSucceed() {
    UUID documentId = UUID.randomUUID();
    BigInteger userId = BigInteger.ONE;
    Document document =
        Document.builder()
            .id(documentId)
            .userId(userId)
            .documentName("test-document")
            .tags(Collections.singletonList("test"))
            .minioPath("/path/to/document")
            .documentUrl("http://localhost/document")
            .fileSize(1024L)
            .fileType("text/plain")
            .updatedAt(Instant.now())
            .createdAt(Instant.now())
            .checksum("checksum123")
            .status("CREATED")
            .build();

    DocumentEntity entity = new DocumentEntity();
    when(documentMapper.toEntity(document)).thenReturn(entity);
    when(documentRepository.save(entity)).thenReturn(entity);
    when(documentMapper.toDomain(entity)).thenReturn(document);

    Document savedDocument = documentRepositoryAdapter.save(document);

    assertNotNull(savedDocument);
    assertEquals(document.getId(), savedDocument.getId());
    verify(documentRepository, times(1)).save(entity);
  }

  @Test
  @DisplayName("Should throw ApplicationException when DataAccessException occurs")
  void save_ShouldThrowApplicationException_WhenDataAccessExceptionOccurs() {
    Document document =
        Document.builder()
            .id(UUID.randomUUID())
            .userId(BigInteger.ONE)
            .documentName("test-document")
            .tags(Collections.singletonList("test"))
            .minioPath("/path/to/document")
            .documentUrl("http://localhost/document")
            .fileSize(1024L)
            .fileType("text/pdf")
            .updatedAt(Instant.now())
            .createdAt(Instant.now())
            .checksum("checksum123")
            .build();

    when(documentMapper.toEntity(any(Document.class))).thenReturn(new DocumentEntity());
    when(documentRepository.save(any(DocumentEntity.class)))
        .thenThrow(new DataAccessResourceFailureException("Database error"));
    ApplicationException exception =
        assertThrows(ApplicationException.class, () -> documentRepositoryAdapter.save(document));

    assertEquals(ErrorCode.DOCUMENT_NOT_CREATED, exception.getErrorCode());
  }
}
