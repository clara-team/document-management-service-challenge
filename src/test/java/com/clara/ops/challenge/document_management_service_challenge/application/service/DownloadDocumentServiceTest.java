package com.clara.ops.challenge.document_management_service_challenge.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DownloadDocumentException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;

class DownloadDocumentServiceTest {

  @Test
  void testDownloadFile_Success() {
    UUID documentId = UUID.randomUUID();
    String minioPath = "test/path";
    String documentName = "test-document.txt";
    InputStream mockInputStream = new ByteArrayInputStream("test content".getBytes());

    Document document =
        Document.builder()
            .minioPath(minioPath)
            .documentName(documentName)
            .checksum("test-checksum")
            .build();

    DocumentRepositoryPort repository = mock(DocumentRepositoryPort.class);
    DocumentStoragePort storage = mock(DocumentStoragePort.class);

    when(repository.findById(documentId)).thenReturn(Optional.of(document));
    when(storage.download(minioPath, documentName)).thenReturn(mockInputStream);

    DownloadDocumentService service = new DownloadDocumentService(repository, storage);

    InputStreamResource result = service.downloadFile(documentId);

    assertNotNull(result);
    assertEquals(documentName, result.getFilename());
  }

  @Test
  void testDownloadFile_DocumentNotFound() {
    UUID documentId = UUID.randomUUID();

    DocumentRepositoryPort repository = mock(DocumentRepositoryPort.class);
    DocumentStoragePort storage = mock(DocumentStoragePort.class);

    when(repository.findById(documentId)).thenReturn(Optional.empty());

    DownloadDocumentService service = new DownloadDocumentService(repository, storage);

    assertThrows(DocumentNotFoundException.class, () -> service.downloadFile(documentId));
  }

  @Test
  void testDownloadFile_DownloadFailure() {
    UUID documentId = UUID.randomUUID();
    String minioPath = "test/path";
    String documentName = "test-document.txt";

    Document document =
        Document.builder().id(documentId).documentUrl(minioPath).documentName(documentName).build();

    DocumentRepositoryPort repository = mock(DocumentRepositoryPort.class);
    DocumentStoragePort storage = mock(DocumentStoragePort.class);

    when(repository.findById(documentId)).thenReturn(Optional.of(document));
    when(storage.download(minioPath, documentName)).thenThrow(new RuntimeException("MinIO error"));

    DownloadDocumentService service = new DownloadDocumentService(repository, storage);

    assertThrows(DownloadDocumentException.class, () -> service.downloadFile(documentId));
  }
}
