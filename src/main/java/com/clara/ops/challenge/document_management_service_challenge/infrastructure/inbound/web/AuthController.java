package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.config.JwtTokenProvider;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AuthController class provides endpoints for managing authentication-related actions. It is a
 * REST controller that handles requests for token generation using user-specific information. This
 * controller leverages the JwtTokenProvider to create JWT tokens.
 *
 * <p>Primary Responsibilities: - Exposes a REST API endpoint to generate a JWT token for
 * authentication.
 *
 * <p>Key Endpoints: - GET /api/v1/auth/token: Provides functionality to generate a JWT token by
 * accepting a user ID as input.
 *
 * <p>Dependencies: - JwtTokenProvider: Responsible for generating JWT tokens based on user-specific
 * details.
 *
 * <p>Annotations: - @RestController: Marks the class as a REST API controller.
 * - @RequestMapping("/api/v1/auth"): Maps requests with the given base path to this controller.
 * - @RequiredArgsConstructor: Generates a constructor for required dependencies.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("/token")
  public ResponseEntity<TokenResponse> generateToken(@RequestParam("userId") String userId) {
    return ResponseEntity.ok(jwtTokenProvider.generateToken(userId));
  }
}
