package com.clara.ops.challenge.document_management_service_challenge.service;

import io.minio.*;
import io.minio.http.Method;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioService {

  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String bucketName;

  public MinioService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public String uploadFile(
      String user, String documentName, InputStream inputStream, long size, String contentType) {
    String objectName = user + "/" + documentName;
    try {
      boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      }

      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                  inputStream, size, -1)
              .contentType(contentType)
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error uploading file to MinIO: ", e);
    }
    return objectName;
  }

  public String getPresignedUrl(String objectName) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(bucketName)
              .object(objectName)
              .expiry(60 * 60) // 1 hour
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error generating pre-signed URL: ", e);
    }
  }
}
