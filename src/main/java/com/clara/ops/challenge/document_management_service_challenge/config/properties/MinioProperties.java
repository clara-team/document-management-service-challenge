package com.clara.ops.challenge.document_management_service_challenge.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
  private String url;
  private String publicUrl;
  private String accessKey;
  private String secretKey;
  private String bucketName;
  private Integer uploadTtl;
  private Integer downloadTtl;
}
