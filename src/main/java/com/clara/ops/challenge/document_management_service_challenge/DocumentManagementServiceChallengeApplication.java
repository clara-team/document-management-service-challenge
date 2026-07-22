package com.clara.ops.challenge.document_management_service_challenge;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class DocumentManagementServiceChallengeApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocumentManagementServiceChallengeApplication.class, args);
  }
}
