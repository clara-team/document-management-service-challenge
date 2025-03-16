package com.clara.ops.challenge.document_management_service_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Document Management Services API")
                .version("1.0.0")
                .description(
                    "Documentary definition of the API with full context of methods, operations,"
                        + " and attributes."));
  }
}
