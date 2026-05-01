package com.cabybara.prolearningplatform.service.auth.impl;

import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.service.auth.RefreshTokenService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisService redisService;

    @Value("${spring.security.jwt.refresh-ttl-ms}")
    private long refreshTtlMs;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private String tokenKey(String token)    { return "auth:refresh:" + token; }
    private String userIndexKey(Long userId) { return "auth:refresh-index:" + userId; }

    @Override
    public String issue(Long userId) {
        String token = generateToken();
        long ttlSeconds = refreshTtlMs / 1000;

        redisService.set(tokenKey(token), userId.toString(), ttlSeconds);

        String idxKey = userIndexKey(userId);
        redisService.sAdd(idxKey, token);
        redisService.expire(idxKey, ttlSeconds);

        return token;
    }

    @Override
    public Long validateAndConsume(String token) {
        String key = tokenKey(token);
        Object stored = redisService.get(key);
        if (stored == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        Long userId = Long.parseLong(stored.toString());

        redisService.delete(key);
        redisService.sRemove(userIndexKey(userId), token);

        return userId;
    }

    @Override
    public void revoke(String token) {
        String key = tokenKey(token);
        Object stored = redisService.get(key);
        if (stored == null) return;

        Long userId = Long.parseLong(stored.toString());
        redisService.delete(key);
        redisService.sRemove(userIndexKey(userId), token);
    }

    @Override
    public void revokeAllForUser(Long userId) {
        String idxKey = userIndexKey(userId);
        Set<String> tokens = redisService.sMembers(idxKey);
        if (tokens.isEmpty()) return;

        List<String> tokenKeys = tokens.stream().map(this::tokenKey).toList();
        redisService.delete(tokenKeys);
        redisService.delete(idxKey);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
