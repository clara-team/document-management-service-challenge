package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
