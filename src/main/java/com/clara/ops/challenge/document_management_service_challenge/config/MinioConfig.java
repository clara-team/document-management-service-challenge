package com.clara.ops.challenge.document_management_service_challenge.config;

import com.clara.ops.challenge.document_management_service_challenge.config.properties.MinioProperties;
import java.net.URI;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class MinioConfig {

  @Bean
  public S3Client s3Client(MinioProperties properties) {
    return S3Client.builder()
        .endpointOverride(URI.create(properties.getUrl()))
        .region(Region.US_EAST_1)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .httpClientBuilder(
            UrlConnectionHttpClient.builder()
                .socketTimeout(Duration.ofMinutes(properties.getUploadTtl())))
        .build();
  }

  @Bean
  public S3Presigner s3Presigner(MinioProperties properties) {
    String presignerUrl =
        properties.getPublicUrl() != null ? properties.getPublicUrl() : properties.getUrl();
    return S3Presigner.builder()
        .endpointOverride(URI.create(presignerUrl))
        .region(Region.US_EAST_1)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
  }
}
