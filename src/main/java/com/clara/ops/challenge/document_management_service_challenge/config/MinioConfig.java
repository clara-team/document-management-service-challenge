package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Value("${minio.region:us-east-1}")
  private String region;

  @Bean
  public MinioClient minioClient() {
    try {
      log.info("Initializing MinIO client with endpoint: {}", endpoint);

      // Initialize MinioClient with the latest builder pattern
      MinioClient minioClient =
          MinioClient.builder()
              .endpoint(endpoint)
              .credentials(accessKey, secretKey)
              .region(region)
              .build();

      // Check if the bucket exists and create it if it doesn't
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

      if (!bucketExists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).region(region).build());
        log.info("Bucket '{}' created successfully in region '{}'", bucketName, region);
      } else {
        log.info("Bucket '{}' already exists", bucketName);
      }

      return minioClient;
    } catch (Exception e) {
      log.error("Error initializing MinIO client: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to initialize MinIO client", e);
    }
  }

  @Bean
  public String bucketName() {
    return bucketName;
  }
}
