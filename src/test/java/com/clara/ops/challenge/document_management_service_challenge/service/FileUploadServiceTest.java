package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.config.properties.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentStorageException;
import java.io.ByteArrayInputStream;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

  @Mock private S3Client s3Client;
  @Mock private S3Presigner s3Presigner;
  @Mock private MinioProperties properties;

  @InjectMocks private FileUploadService fileUploadService;

  @BeforeEach
  void setUp() {
    when(properties.getBucketName()).thenReturn("test-bucket");
  }

  @Test
  @DisplayName(
      "uploadFile: calls putObject with correct bucket, key, contentType and contentLength")
  void uploadFileCallsPutObjectWithCorrectRequest() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(file.getContentType()).thenReturn("application/pdf");
    when(file.getSize()).thenReturn(1024L);

    fileUploadService.uploadFile(file, "user/doc.pdf");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
    PutObjectRequest request = captor.getValue();
    assertThat(request.bucket()).isEqualTo("test-bucket");
    assertThat(request.key()).isEqualTo("user/doc.pdf");
    assertThat(request.contentType()).isEqualTo("application/pdf");
    assertThat(request.contentLength()).isEqualTo(1024L);
  }

  @Test
  @DisplayName("uploadFile: creates bucket when headBucket returns 404 then proceeds with upload")
  void uploadFileCreatesBucketWhenNotFound() throws Exception {
    S3Exception notFound =
        (S3Exception) S3Exception.builder().statusCode(404).message("Not Found").build();
    when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(notFound);
    MultipartFile file = mock(MultipartFile.class);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(file.getSize()).thenReturn(0L);

    fileUploadService.uploadFile(file, "user/doc.pdf");

    ArgumentCaptor<CreateBucketRequest> captor = ArgumentCaptor.forClass(CreateBucketRequest.class);
    verify(s3Client).createBucket(captor.capture());
    assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName(
      "uploadFile: throws DocumentStorageException when headBucket fails with a non-404 error")
  void uploadFileThrowsDocumentStorageExceptionWhenBucketAccessFails() {
    S3Exception forbidden =
        (S3Exception) S3Exception.builder().statusCode(403).message("Forbidden").build();
    when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(forbidden);

    assertThatThrownBy(
            () -> fileUploadService.uploadFile(mock(MultipartFile.class), "user/doc.pdf"))
        .isInstanceOf(DocumentStorageException.class)
        .hasMessage("Failed to access bucket");

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName(
      "uploadFile: throws DocumentStorageException when headBucket fails with a generic exception")
  void uploadFile_throwsDocumentStorageExceptionOnGenericBucketError() {
    when(s3Client.headBucket(any(HeadBucketRequest.class)))
        .thenThrow(new RuntimeException("connection refused"));

    assertThatThrownBy(
            () -> fileUploadService.uploadFile(mock(MultipartFile.class), "user/doc.pdf"))
        .isInstanceOf(DocumentStorageException.class)
        .hasMessage("Failed to access bucket");

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("uploadFile: throws DocumentStorageException when putObject fails")
  void uploadFileThrowsDocumentStorageExceptionWhenPutObjectFails() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(file.getSize()).thenReturn(0L);
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(new RuntimeException("upload failed"));

    assertThatThrownBy(() -> fileUploadService.uploadFile(file, "user/doc.pdf"))
        .isInstanceOf(DocumentStorageException.class)
        .hasMessage("Failed to upload file");
  }

  @Test
  @DisplayName("getDownloadLink: returns the presigned URL as string")
  void getDownloadLinkReturnsPresignedUrl() throws Exception {
    when(properties.getDownloadTtl()).thenReturn(30);
    PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("https://minio.example.com/test-bucket/user/doc.pdf"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

    String url = fileUploadService.getDownloadLink("user/doc.pdf");

    assertThat(url).isEqualTo("https://minio.example.com/test-bucket/user/doc.pdf");
  }

  @Test
  @DisplayName("getDownloadLink: throws DocumentStorageException when presigner fails")
  void getDownloadLinkThrowsDocumentStorageExceptionWhenPresignerFails() {
    when(properties.getDownloadTtl()).thenReturn(30);
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenThrow(new RuntimeException("presigner error"));

    assertThatThrownBy(() -> fileUploadService.getDownloadLink("user/doc.pdf"))
        .isInstanceOf(DocumentStorageException.class)
        .hasMessage("Error obtaining download link");
  }
}
