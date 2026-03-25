package com.clara.ops.challenge.document_management_service_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo(){
        return new Info()
                .title("Management Service API")
                .description("All Management Service functionalities")
                .version("v0.0.1")
                .contact(contact());
    }

    private Contact contact(){
        return new Contact()
                .name("Marcelo de Souza Santa Rosa")
                .email("marcelo_santarosa@outlook.com");
    }

}
