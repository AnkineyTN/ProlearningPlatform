package com.cabybara.prolearningplatform.service.auth.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.JwtService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    public static final String BLACKLIST_JTI_KEY_PREFIX = "auth:blacklist-jti:";

    private final RedisService redisService;
    private final UserService userService;

    @Value("${spring.security.jwt.secret}")
    private String JWT_SECRET;

    @Value("${spring.security.jwt.access-ttl-ms}")
    private Long JWT_EXPIRATION;

    @Override
    public String generateToken(Authentication authentication) {
        Map<String, Object> privateClaims = new HashMap<>();
        User user = (User) authentication.getPrincipal();

        privateClaims.put("id", user.getId());
        privateClaims.put("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        return createToken(privateClaims, authentication.getName());
    }

    @Override
    public String generateToken(String username) {
        Map<String, Object> privateClaims = new HashMap<>();
        User user = (User) userService.loadUserByUsername(username);

        privateClaims.put("id", user.getId());
        privateClaims.put("roles", user.getRoles().stream().map(Authority::getAuthority).toList());
        return createToken(privateClaims, username);
    }

    private String createToken(Map<String, Object> privateClaims, String email) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .claims(privateClaims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Key getSignKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        return parse(token).get("id", Long.class);
    }

    private io.jsonwebtoken.Claims parse(String token) {
        JwtParser jwtParser = Jwts.parser()
                .setSigningKey(getSignKey())
                .build();
        return jwtParser.parseClaimsJws(token).getBody();
    }

    @Override
    public Boolean validateToken(String token) {
        io.jsonwebtoken.Claims claims;
        try {
            claims = parse(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }

        if (!claims.getExpiration().after(new Date())) {
            return false;
        }

        String jti = claims.getId();
        return jti == null || !redisService.hasKey(BLACKLIST_JTI_KEY_PREFIX + jti);
    }

    @Override
    public void blacklistAccessTokenByJti(String jti, long ttlSeconds) {
        if (jti == null || ttlSeconds <= 0) return;
        redisService.set(BLACKLIST_JTI_KEY_PREFIX + jti, "1", ttlSeconds);
    }

    @Override
    public DecodedJWT decodeGoogleIdToken(String tokenId) {
        return JWT.decode(tokenId);
    }
}
