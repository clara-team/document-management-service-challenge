package com.clara.ops.challenge.document_management_service_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
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

    @Bean
    public OpenApiCustomizer globalResponseOpenApiCustomiser() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.getResponses().addApiResponse("400", new ApiResponse().description("Bad Request"));
                    operation.getResponses().addApiResponse("401", new ApiResponse().description("Unauthorized"));
                    operation.getResponses().addApiResponse("403", new ApiResponse().description("Forbidden"));
                    operation.getResponses().addApiResponse("404", new ApiResponse().description("Not Found"));
                    operation.getResponses().addApiResponse("409", new ApiResponse().description("Conflict"));
                    operation.getResponses().addApiResponse("500", new ApiResponse().description("Internal Server Error"));
                })
        );
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
