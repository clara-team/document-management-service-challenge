package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for Minio client. */
@Configuration
public class MinioConfig {

  @Value("${minio.endpoint}")
  private String minioUrl;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder().endpoint(minioUrl).credentials(accessKey, secretKey).build();
  }
}
