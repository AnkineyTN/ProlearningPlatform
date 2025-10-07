package com.cabybara.prolearningplatform.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.cabybara.prolearningplatform.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.SignatureAlgorithm;

public interface JwtService {
    String generateToken(String username);

    String generateToken(Authentication authentication);

    String extractEmail(String token);

    Boolean validateToken(String token);

    void blacklistToken(String token);

    Boolean isTokenBlacklisted(String token);
}
