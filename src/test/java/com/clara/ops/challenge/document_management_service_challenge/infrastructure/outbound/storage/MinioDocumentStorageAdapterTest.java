package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.StorageException;
import io.minio.MinioClient;
import io.minio.errors.*;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MinioDocumentStorageAdapterTest {

  @Mock private MinioClient minioClient;
  @Mock private MinioProperties minioProperties;

  private MinioDocumentStorageAdapter adapter;
  private MockMultipartFile validFile;
  private MockMultipartFile invalidFile;

  @BeforeEach
  void setup() {

    minioProperties.setBucketName("test-bucket");
    adapter = new MinioDocumentStorageAdapter(minioClient, minioProperties);
    validFile =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "fake-pdf-content".getBytes());
    invalidFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
  }

  @Test
  @DisplayName("Should upload file successfully")
  void whenUploadValidFile_thenReturnSuccess() throws Exception {

    MinioClient minioClient = mock(MinioClient.class);
    MinioProperties minioProperties = new MinioProperties();
    minioProperties.setBucketName("test-bucket");
    MinioDocumentStorageAdapter adapter =
        new MinioDocumentStorageAdapter(minioClient, minioProperties);

    MultipartFile file =
        new MockMultipartFile("test.txt", "test.txt", "text/plain", "Test content".getBytes());
    String userId = "user123";
    String expectedObjectPath = "user123/test.txt";

    String actualObjectPath = adapter.uploadFile(file, userId);

    assertEquals(expectedObjectPath, actualObjectPath);
    verify(minioClient, times(1))
        .putObject(
            argThat(
                args -> {
                  try {
                    return args.bucket().equals("test-bucket")
                        && args.object().equals(expectedObjectPath)
                        && args.contentType().equals("text/plain");
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }));
  }

  @Test
  @DisplayName("Should throw exception when upload fails")
  void shouldThrowExceptionWhenUploadFails() throws Exception {
    when(minioProperties.getBucketName()).thenReturn("test-bucket");
    when(minioClient.putObject(any())).thenThrow(new RuntimeException("Simulated MinIO failure"));
    assertThrows(StorageException.class, () -> adapter.uploadFile(validFile, "user123"));
  }

  @Test
  @DisplayName("Should throw exception for invalid file")
  void shouldThrowExceptionForInvalidFile() {
    assertThrows(StorageException.class, () -> adapter.uploadFile(invalidFile, "user123"));
  }
}
