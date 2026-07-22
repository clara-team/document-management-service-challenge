package com.clara.ops.challenge.document_management_service_challenge.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.validator.DocumentValidator;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.ChecksumException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentValidationException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

class UploadDocumentServiceTest {

  private final DocumentRepositoryPort repository = mock(DocumentRepositoryPort.class);
  private final DocumentStoragePort storage = mock(DocumentStoragePort.class);
  private final DocumentValidator validator = mock(DocumentValidator.class);
  private final UploadDocumentService service =
      new UploadDocumentService(storage, repository, validator);

  @Test
  @DisplayName("Upload OK → returns document with ID")
  void testUploadSuccess() {

    ArrayList<String> tags = new ArrayList<>();
    tags.add("invoice");
    tags.add("confidential");

    MockMultipartFile file =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy".getBytes());
    Document document = Document.builder().documentName("test.pdf").build();
    DocumentMetadata metadata = new DocumentMetadata(tags, "12");

    when(repository.existsByUserIdAndChecksum(any(), any())).thenReturn(false);
    try {
      when(storage.uploadFile(any(), any())).thenReturn("minio/path/test.pdf");
    } catch (IOException e) {
      fail("IOException should not occur during mock setup: " + e.getMessage());
    }
    when(repository.save(any(Document.class))).thenReturn(document);

    var result = service.upload(file, metadata);

    assertNotNull(result);
    verify(repository).save(any(Document.class));
  }

  @Test
  @DisplayName("Upload duplicate document → throws validation exception")
  void testUploadDuplicateDocumentThrowsValidationException() {

    ArrayList<String> tags = new ArrayList<>();
    tags.add("test");
    tags.add("confidential");

    MockMultipartFile file =
        new MockMultipartFile("file", "dup.pdf", "application/pdf", "duplicate".getBytes());
    DocumentMetadata metadata = new DocumentMetadata(tags, "1");

    when(repository.existsByUserIdAndChecksum(any(), any())).thenReturn(true);

    assertThrows(DocumentValidationException.class, () -> service.upload(file, metadata));
  }

  @Test
  @DisplayName("Checksum fails → throws ChecksumException")
  void testUploadThrowsChecksumException() {

    ArrayList<String> tags = new ArrayList<>();
    tags.add("test");
    tags.add("confidential-1");

    UploadDocumentService serviceSpy = Mockito.spy(service);
    MockMultipartFile file =
        new MockMultipartFile("file", "fail.pdf", "application/pdf", "bad".getBytes());
    DocumentMetadata metadata = new DocumentMetadata((ArrayList<String>) tags, "2");
    doThrow(new ChecksumException("invalid")).when(serviceSpy).upload(file, metadata);

    assertThrows(ChecksumException.class, () -> serviceSpy.upload(file, metadata));
  }

  @Test
  @DisplayName("Upload fails after storage → rollback deletes file")
  void testUploadFileFailureRollback() throws Exception {

    ArrayList<String> tags = new ArrayList<>();
    tags.add("test");
    tags.add("test-1");

    MockMultipartFile file =
        new MockMultipartFile("file", "rollback.pdf", "application/pdf", "bad".getBytes());
    DocumentMetadata metadata = new DocumentMetadata((ArrayList<String>) tags, "11");

    when(repository.existsByUserIdAndChecksum(any(), any())).thenReturn(false);
    when(storage.uploadFile(any(), any())).thenReturn("minio/path/rollback.pdf");
    when(repository.save(any(Document.class))).thenThrow(new RuntimeException("DB save failed"));

    assertThrows(RuntimeException.class, () -> service.upload(file, metadata));

    verify(storage).delete("minio/path/rollback.pdf");
  }
}
