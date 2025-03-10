package com.clara.ops.challenge.document_management_service_challenge.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Bean
  public GroupedOpenApi controllerApi() {
    return GroupedOpenApi.builder()
        .group("controller-api")
        .packagesToScan("com.clara.ops.challenge.document_management_service_challenge.controller")
        .build();
  }
}
