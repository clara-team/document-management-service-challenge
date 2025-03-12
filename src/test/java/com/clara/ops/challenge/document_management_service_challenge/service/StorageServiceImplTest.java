package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.service.impl.StorageServiceImpl;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class StorageServiceImplTest {

  @Mock private MinioClient minioClient;

  @InjectMocks private StorageServiceImpl storageService;

  private MultipartFile testFile;
  private String bucketName = "document-bucket";
  private int urlExpiryTime = 30;

  @BeforeEach
  void setUp() throws Exception {
    // Set properties using reflection since they're normally injected
    ReflectionTestUtils.setField(storageService, "bucketName", bucketName);
    ReflectionTestUtils.setField(storageService, "urlExpiryTime", urlExpiryTime);

    // Set up test file
    testFile =
        new MockMultipartFile(
            "file", "test-document.pdf", "application/pdf", "Test PDF content".getBytes());
  }

  @Test
  void uploadFile_Success() throws Exception {
    // Call service method
    String result = storageService.uploadFile("test-user", "test-document.pdf", testFile);

    // Verify results
    assertThat(result).isEqualTo("test-user/test-document.pdf");

    // Verify the MinioClient was called with the appropriate arguments
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void uploadFile_MinioException() throws Exception {
    // Setup mock behavior
    when(minioClient.putObject(any(PutObjectArgs.class)))
        .thenThrow(new RuntimeException("MinIO connection error"));

    // Call service method and verify exception
    assertThatThrownBy(() -> storageService.uploadFile("test-user", "test-document.pdf", testFile))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to upload file");
  }

  @Test
  void generatePresignedUrl_Success() throws Exception {
    // Setup mock behavior
    String expectedUrl =
        "https://minio-endpoint/document-bucket/test-user/test-document.pdf?token=xyz";
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn(expectedUrl);

    // Call service method
    String result = storageService.generatePresignedUrl("test-user/test-document.pdf");

    // Verify results
    assertThat(result).isEqualTo(expectedUrl);

    // Verify the MinioClient was called
    verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void generatePresignedUrl_MinioException() throws Exception {
    // Setup mock behavior
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenThrow(new RuntimeException("MinIO connection error"));

    // Call service method and verify exception
    assertThatThrownBy(() -> storageService.generatePresignedUrl("test-user/test-document.pdf"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to generate pre-signed URL");
  }
}
