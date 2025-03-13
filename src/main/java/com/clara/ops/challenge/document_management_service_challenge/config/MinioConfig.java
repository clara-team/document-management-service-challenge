package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
  private String endpoint;
  private Integer portApi;
  private boolean secure;
  private String rootUser;
  private String rootPassword;
  private String bucketName;
  private long fileSize;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder()
        .endpoint(endpoint, portApi, secure)
        .credentials(rootUser, rootPassword)
        .build();
  }
}
