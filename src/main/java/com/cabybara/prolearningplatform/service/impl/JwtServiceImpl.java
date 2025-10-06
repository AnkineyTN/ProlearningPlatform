package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.JwtService;
import com.cabybara.prolearningplatform.service.RedisService;
import com.cabybara.prolearningplatform.service.UserService;
import io.jsonwebtoken.JwtParser;
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

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final RedisService redisService;
    @Value("${spring.security.jwt.secret}")
    private String JWT_SECRET;

    @Value("${spring.security.jwt.access-ttl-ms}")
    private Long JWT_EXPIRATION;

    private final UserService userService;

    @Override
    public String generateToken(Authentication authentication) {
        Map<String, Object> privateClaims = new HashMap<>();
        privateClaims.put("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        return createToken(privateClaims, authentication.getName());
    }

    @Override
    public String generateToken(String username) {
        Map<String, Object> privateClaims = new HashMap<>();
        User user = (User) userService.loadUserByUsername(username);

        privateClaims.put("roles", user.getRoles());
        return createToken(privateClaims, username);
    }

    private String createToken(Map<String, Object> privateClaims, String email) {
        return Jwts.builder()
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
        JwtParser jwtParser = Jwts.parser()
                .setSigningKey(getSignKey())
                .build();

        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Date extractExpiration(String token) {
        JwtParser jwtParser = Jwts.parser()
                .setSigningKey(getSignKey())
                .build();

        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    @Override
    public Boolean validateToken(String token) {
        Date expiration = extractExpiration(token);

        return expiration.after(new Date());
    }

    @Override
    public void backlistToken(String token) {
        long now = System.currentTimeMillis();
        long expirationTime = extractExpiration(token).getTime();
        long ttl = (expirationTime - now) / 1000;
        redisService.set(token, "backlisted", ttl);
    }

    @Override
    public Boolean isTokenBacklisted(String token) {
        return redisService.hasKey(token);
    }
}
