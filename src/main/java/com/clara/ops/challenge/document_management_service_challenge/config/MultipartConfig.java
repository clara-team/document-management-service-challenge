package com.clara.ops.challenge.document_management_service_challenge.config;

import jakarta.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Slf4j
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

    // Max MB size of file
    factory.setMaxFileSize(DataSize.ofMegabytes(500));
    // Max MB size of request
    factory.setMaxRequestSize(DataSize.ofMegabytes(520));
    // Determines in KB when to start writing in disk
    factory.setFileSizeThreshold(DataSize.ofKilobytes(512));

    MultipartConfigElement config = factory.createMultipartConfig();
    log.info(
        "Multipart config created: maxFileSize={}, maxRequestSize={}, fileSizeThreshold={}",
        config.getMaxFileSize(),
        config.getMaxRequestSize(),
        DataSize.ofKilobytes(512));
    return config;
  }
}
