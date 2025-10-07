package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up Minio Client.
 *
 * <p>This class creates a `MinioClient` bean to interact with a Minio server for file storage. The
 * client is configured using the properties
 */
@Configuration
public class MinioConfig {
  @Bean
  public MinioClient minioClient(MinioProperties minioProperties) {
    return MinioClient.builder()
        .endpoint(minioProperties.getUrl())
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .build();
  }
}
