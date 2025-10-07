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

/**
 * The JwtTokenProvider class is responsible for the creation and management of JSON Web Tokens
 * (JWT). It generates tokens containing user-specific information to be used for authentication and
 * authorization. This class employs a secret key-based signing mechanism to ensure the integrity
 * and authenticity of the tokens.
 *
 * <p>Key Features: - Generates JWT token using the HMAC SHA-512 signing algorithm. - Sets essential
 * JWT claims including subject, roles, issued date, and expiration time. - Uses `JwtProperties` to
 * retrieve the secret key required for token signing.
 *
 * <p>Dependencies: - JwtProperties: Configuration class for managing JWT-related properties such as
 * the secret key. - TokenResponse: Data structure to encapsulate JWT-related details like token
 * string, token type, and expiration duration.
 */
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
