package com.clara.ops.challenge.document_management_service_challenge.util;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class MinioUtil {
  private final MinioClient minioClient;
  private final MinioConfig minioConfig;

  @SneakyThrows
  public void putObject(
      String bucketName, MultipartFile multipartFile, String filename, String fileType) {
    InputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes());
    minioClient.putObject(
        PutObjectArgs.builder().bucket(bucketName).object(filename).stream(
                inputStream, multipartFile.getSize(), minioConfig.getFileSize())
            .contentType(fileType)
            .build());
  }

  @SneakyThrows
  public String getObjectUrl(String bucketName, String objectName) {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucketName)
            .object(objectName)
            .expiry(2, TimeUnit.MINUTES)
            .build());
  }

  @SneakyThrows
  public boolean bucketExists(String bucketName) {
    return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
  }

  @SneakyThrows
  public void makeBucket(String bucketName) {
    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
  }
}
