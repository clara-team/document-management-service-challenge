package com.clara.ops.challenge.document_management_service_challenge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioProps {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
