package com.clara.ops.challenge.document_management_service_challenge.service.impl;

import com.clara.ops.challenge.document_management_service_challenge.service.StorageService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

  private final MinioClient minioClient;
  private final String bucketName;

  @Value("${minio.url-expiry-time}")
  private int urlExpiryTime;

  @Autowired
  public StorageServiceImpl(MinioClient minioClient, String bucketName) {
    this.minioClient = minioClient;
    this.bucketName = bucketName;
  }

  /**
   * Upload a file to MinIO storage
   *
   * @param userId The user ID
   * @param documentName The name of the document
   * @param file The file to upload
   * @return The path where the file was stored in MinIO
   */
  public String uploadFile(String userId, String documentName, MultipartFile file) {
    try {
      String objectPath = String.format("%s/%s", userId, documentName);

      // Use BufferedInputStream to avoid loading the entire file into memory
      try (InputStream fileInputStream = new BufferedInputStream(file.getInputStream())) {
        PutObjectArgs putObjectArgs =
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .contentType(file.getContentType())
                .stream(fileInputStream, file.getSize(), -1)
                .build();

        minioClient.putObject(putObjectArgs);

        log.info(
            "File {} uploaded successfully for user {} with size {}",
            documentName,
            userId,
            file.getSize());
        return objectPath;
      }
    } catch (Exception e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  /**
   * Generate a pre-signed URL for downloading a file
   *
   * @param objectPath The path to the object in MinIO
   * @return A pre-signed URL for downloading the file
   */
  public String generatePresignedUrl(String objectPath) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .bucket(bucketName)
              .object(objectPath)
              .method(Method.GET)
              .expiry(urlExpiryTime, TimeUnit.MINUTES)
              .build());
    } catch (Exception e) {
      log.error("Error generating pre-signed URL: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate pre-signed URL", e);
    }
  }
}
