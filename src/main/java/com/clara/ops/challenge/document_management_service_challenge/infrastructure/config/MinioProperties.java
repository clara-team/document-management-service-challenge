package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for interacting with a Minio server.
 *
 * <p>This class holds the necessary configurations for connecting to a Minio server, such as the
 * URL, access key, secret key, and the default bucket name. These properties are loaded from the
 * application configuration using the prefix "minio". The class is primarily used to supply
 * configuration values for setting up a Minio client.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
  private String url;
  private String accessKey;
  private String secretKey;
  private String bucketName;
}
