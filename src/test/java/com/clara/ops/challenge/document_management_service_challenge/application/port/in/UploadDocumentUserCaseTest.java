package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class UploadDocumentUserCaseTest {

  @Test
  void shouldUploadDocumentSuccessfully() throws IOException {
    // Arrange
    UploadDocumentUserCase uploadDocumentUserCase = mock(UploadDocumentUserCase.class);

    MultipartFile file = mock(MultipartFile.class);
    DocumentMetadata metadata =
        new DocumentMetadata(new ArrayList<>(List.of("tag1", "tag2")), "user-123");
    UploadDocumentResponse expectedResponse =
        UploadDocumentResponse.builder().documentId(UUID.randomUUID()).build();

    when(uploadDocumentUserCase.upload(file, metadata)).thenReturn(expectedResponse);

    // Act
    UploadDocumentResponse actualResponse = uploadDocumentUserCase.upload(file, metadata);

    // Assert
    assertEquals(expectedResponse, actualResponse);
    verify(uploadDocumentUserCase, times(1)).upload(file, metadata);
  }

  @Test
  void shouldThrowIOExceptionWhenFileUploadFails() throws IOException {
    // Arrange
    UploadDocumentUserCase uploadDocumentUserCase = mock(UploadDocumentUserCase.class);

    MultipartFile file = mock(MultipartFile.class);
    DocumentMetadata metadata =
        new DocumentMetadata(new ArrayList<>(List.of("tag1", "tag2")), "user-123");

    when(uploadDocumentUserCase.upload(file, metadata)).thenThrow(IOException.class);

    // Act & Assert
    assertThrows(IOException.class, () -> uploadDocumentUserCase.upload(file, metadata));
    verify(uploadDocumentUserCase, times(1)).upload(file, metadata);
  }
}
