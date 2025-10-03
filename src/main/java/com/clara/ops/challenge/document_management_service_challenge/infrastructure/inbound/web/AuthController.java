package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.config.JwtTokenProvider;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
