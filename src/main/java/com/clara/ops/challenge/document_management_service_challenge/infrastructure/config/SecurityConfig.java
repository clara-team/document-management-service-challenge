package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Simplified security configuration for local development.
 *
 * <p>This setup allows unrestricted access to actuator and document endpoints, and provides a
 * placeholder for JWT support to be reactivated later.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Dummy bean so that Actuator can autowire HttpSecurity and avoid "No qualifying bean" errors.
   */
  /** Actuator endpoints — allow health/info checks with no authentication. */
  @Bean
  @Order(1)
  public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  /**
   * Application endpoints — open for development. Later you can switch to authenticated() and add
   * JWT.
   */
  @Bean
  @Order(2)
  public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/v1/documents/**",
                        "/api/v1/document-management/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .denyAll());
    return http.build();
  }

  // Commented out for now — JWT module not implemented yet
  // @Bean
  // public JwtDecoder jwtDecoder() {
  //   SecretKey key =
  // Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  //   return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
  // }
}
