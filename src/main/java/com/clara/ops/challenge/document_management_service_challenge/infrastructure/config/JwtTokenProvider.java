package com.clara.ops.challenge.document_management_service_challenge.infrastructure.config;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;
  private static final long EXPIRATION_MS = 3600000;
  private static final SignatureAlgorithm SIGNING_ALGORITHM = SignatureAlgorithm.HS512;

  public TokenResponse generateToken(String userId) {
    Key signingKey = getSigningKey();
    String jwtToken =
        Jwts.builder()
            .setSubject(userId)
            .claim("roles", List.of("ROLE_USER"))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
            .signWith(signingKey, SIGNING_ALGORITHM)
            .compact();

    long expirationInSeconds = EXPIRATION_MS / 1000;
    return new TokenResponse(jwtToken, "Bearer", expirationInSeconds);
  }

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
  }
}
