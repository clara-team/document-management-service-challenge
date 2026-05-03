package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.properties.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentStorageException;
import java.io.InputStream;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final MinioProperties properties;

  public void uploadFile(MultipartFile file, String objectKey) {
    validateBucketExists();
    try (InputStream inputStream = file.getInputStream()) {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(properties.getBucketName())
              .key(objectKey)
              .contentType(file.getContentType())
              .contentLength(file.getSize())
              .build(),
          RequestBody.fromInputStream(inputStream, file.getSize()));
    } catch (Exception e) {
      log.error("Error while uploading file: {}", objectKey, e);
      throw new DocumentStorageException("Failed to upload file");
    }
  }

  public String getDownloadLink(String objectKey) {
    try {
      return s3Presigner
          .presignGetObject(
              GetObjectPresignRequest.builder()
                  .signatureDuration(Duration.ofMinutes(properties.getDownloadTtl()))
                  .getObjectRequest(
                      GetObjectRequest.builder()
                          .bucket(properties.getBucketName())
                          .key(objectKey)
                          .build())
                  .build())
          .url()
          .toString();
    } catch (Exception e) {
      log.error("Error obtaining url link for {}", objectKey, e);
      throw new DocumentStorageException("Error obtaining download link");
    }
  }

  private void validateBucketExists() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucketName()).build());
    } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
      if (e.statusCode() == 404) {
        s3Client.createBucket(
            CreateBucketRequest.builder().bucket(properties.getBucketName()).build());
        return;
      }
      log.error("Error accessing bucket: {}", properties.getBucketName(), e);
      throw new DocumentStorageException("Failed to access bucket");
    } catch (Exception e) {
      log.error("Error accessing bucket: {}", properties.getBucketName(), e);
      throw new DocumentStorageException("Failed to access bucket");
    }
  }
}
