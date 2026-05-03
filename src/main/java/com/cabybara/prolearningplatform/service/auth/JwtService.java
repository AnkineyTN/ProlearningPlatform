package com.cabybara.prolearningplatform.service.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.Authentication;

public interface JwtService {
    String generateToken(String username);

    String generateToken(Authentication authentication);

    String extractEmail(String token);

    Long extractUserId(String token);

    Boolean validateToken(String token);

    void blacklistAccessTokenByJti(String jti, long ttlSeconds);

    DecodedJWT decodeGoogleIdToken(String tokenId);
}
