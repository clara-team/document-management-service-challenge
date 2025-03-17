package com.clara.ops.challenge.document_management_service_challenge.util;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {
  private final MinioClient minioClient;

  @SneakyThrows
  public void putObject(
      String bucketName, InputStream inputStream, String filename, String fileType, Long partSize) {
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(filename).stream(
                  inputStream, inputStream.available(), partSize)
              .contentType(fileType)
              .build());
    } catch (MinioException e) {
      log.info("Exception MinioException occurred in method putObject: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception MinioException occurred in method putObject",
          e);
    } catch (Exception e) {
      log.info("General exception occurred in method putObject: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "General exception occurred in method putObject", e);
    }
  }

  @SneakyThrows
  public String getObjectUrl(String bucketName, String objectName) {
    String url = null;
    try {
      url =
          minioClient.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.GET)
                  .bucket(bucketName)
                  .object(objectName)
                  .expiry(1, TimeUnit.MINUTES)
                  .build());
    } catch (MinioException e) {
      log.info("Exception MinioException occurred in method getObjectUrl: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception MinioException occurred in method getObjectUrl",
          e);
    } catch (Exception e) {
      log.info("General exception occurred in method getObjectUrl: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "General exception occurred in method getObjectUrl", e);
    }
    return url;
  }

  @SneakyThrows
  public boolean bucketExists(String bucketName) {
    boolean found = false;
    try {
      found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    } catch (MinioException e) {
      log.info("Exception MinioException occurred in method bucketExists: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception MinioException occurred in method bucketExists",
          e);
    } catch (Exception e) {
      log.info("General exception occurred in method bucketExists: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "General exception occurred in method bucketExists", e);
    }
    return found;
  }

  @SneakyThrows
  public void makeBucket(String bucketName) {
    try {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    } catch (MinioException e) {
      log.info("Exception MinioException occurred in method makeBucket: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Exception MinioException occurred in method makeBucket",
          e);
    } catch (Exception e) {
      log.info("General exception occurred in method makeBucket: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "General exception occurred in method makeBucket", e);
    }
  }
}
