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

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${spring.security.jwtSecret}")
    private String JWT_SECRET;

    @Value("${spring.security.JWT_EXPIRATION}")
    private Long JWT_EXPIRATION;

    private final UserService userService;

    public String generateToken(Authentication authentication) {
        Map<String, Object> privateClaims = new HashMap<>();
        privateClaims.put("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        return createToken(privateClaims, authentication.getName());
    }

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

    public Boolean validateToken(String token) {
        Date expiration = extractExpiration(token);

        return expiration.after(new Date());
    }

}
