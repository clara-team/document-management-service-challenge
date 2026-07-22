package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JSON Web Token (JWT).
 *
 * <p>This class holds the configuration settings for JWT, such as the secret key. The properties
 * are loaded from the application configuration using the prefix "security.jwt". It is used for
 * setting up and managing JWT-related functionalities, including token generation and validation.
 *
 * <p>A logger is included to log the initialization of this configuration.
 */
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class JwtProperties {
  private final Logger logger = LoggerFactory.getLogger(JwtProperties.class);

  private String secret;

  @PostConstruct
  public void init() {
    logger.info("Loaded JwtProperties: {}", secret);
  }
}
