package com.clara.ops.challenge.document_management_service_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Document Management API")
                .version("1.0.0")
                .description("API for document management"));
  }

  /** Bean post-processor that prevents the Kotlin customizer from loading */
  @Bean
  public BeanPostProcessor kotlinCustomizerSuppressor() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // Prevent the Kotlin customizer from initializing
        if (bean.getClass().getName().contains("KotlinDeprecatedPropertyCustomizer")) {
          return null; // Effectively removing the bean
        }
        return bean;
      }
    };
  }
}
