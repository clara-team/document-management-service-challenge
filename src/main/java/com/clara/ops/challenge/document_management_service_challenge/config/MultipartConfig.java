package com.clara.ops.challenge.document_management_service_challenge.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

  /**
   * Configure multipart settings for handling large file uploads efficiently This is crucial to
   * stay within the 50MB memory constraint while handling files up to 500MB by ensuring files are
   * written to disk
   */
  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();

    factory.setMaxFileSize(DataSize.ofMegabytes(500));
    factory.setMaxRequestSize(DataSize.ofMegabytes(520));

    factory.setFileSizeThreshold(DataSize.ofKilobytes(512));

    return factory.createMultipartConfig();
  }
}
