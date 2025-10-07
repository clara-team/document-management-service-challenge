package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.storage;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.StorageException;
import io.minio.*;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adapter class for interacting with MinIO as a document storage solution. Implements {@link
 * DocumentStoragePort} to provide concrete functionality for file upload, download, deletion, and
 * generating presigned URLs.
 *
 * <p>This class relies on the MinIO client and configuration properties for managing bucket storage
 * operations.
 *
 * <p>Features include: - Uploading files to a MinIO bucket under a specific user path. -
 * Downloading files from a MinIO bucket. - Deleting files from a MinIO bucket. - Generating
 * presigned URLs for accessing files stored in MinIO.
 *
 * <p>The implementation ensures the bucket's existence during upload operations and handles
 * exceptions by logging errors and rethrowing custom {@link StorageException}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioDocumentStorageAdapter implements DocumentStoragePort {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;

  @Override
  public String uploadFile(MultipartFile file, String userId) {
    String bucketName = minioProperties.getBucketName();
    String objectPath = String.format("%s/%s", userId, file.getOriginalFilename());

    ensureBucketExists(bucketName);

    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(objectPath).stream(
                  inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      log.info("File '{}' uploaded successfully to bucket '{}'", objectPath, bucketName);

      return objectPath;

    } catch (Exception e) {
      log.error(
          "Failed to upload file '{}' to bucket '{}': {}",
          objectPath,
          bucketName,
          e.getMessage(),
          e);
      throw new StorageException("Failed to upload file to MinIO", e);
    }
  }

  @Override
  public InputStream download(String path, String filename) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(minioProperties.getBucketName()).object(path).build());
    } catch (Exception e) {
      log.error("Failed to download file '{}' from MinIO: {}", path, e.getMessage(), e);
      throw new StorageException("Failed to download file from MinIO", e);
    }
  }

  @Override
  public void delete(String path) {
    try {
      StatObjectResponse stat =
          minioClient.statObject(
              StatObjectArgs.builder()
                  .bucket(minioProperties.getBucketName())
                  .object(path)
                  .build());
      if (stat != null) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(path)
                .build());
        log.info("File '{}' deleted successfully from MinIO", path);

      } else {
        log.warn("MinIO: Object '{}' not found, nothing to delete.", path);
      }
    } catch (Exception e) {
      log.error("MinIO: Unexpected error deleting object '{}': {}", path, e.getMessage(), e);
    }
  }

  private void ensureBucketExists(String bucketName) {
    try {
      boolean exists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!exists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        log.info("Bucket '{}' created successfully", bucketName);
      }
    } catch (Exception e) {
      log.error("Failed to validate/create bucket '{}': {}", bucketName, e.getMessage(), e);
      throw new StorageException("Failed to validate or create bucket", e);
    }
  }

  public String generatePresignedUrl(String path) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(minioProperties.getBucketName())
              .object(path)
              .expiry(1, TimeUnit.DAYS)
              .build());
    } catch (Exception e) {
      log.error("Failed to generate presigned url for path '{}': {}", path, e.getMessage(), e);
      throw new StorageException("Failed to generate presigned url", e);
    }
  }
}
