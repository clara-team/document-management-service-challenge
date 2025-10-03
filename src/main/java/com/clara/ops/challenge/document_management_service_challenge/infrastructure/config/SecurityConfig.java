package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProperties jwtProperties;

  @Bean
  @Order(1)
  public SecurityFilterChain authEndpoints(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/v1/auth/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain appEndpoints(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/documents/**").authenticated().anyRequest().denyAll())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    return NimbusJwtDecoder.withSecretKey(key).build();
  }
}
