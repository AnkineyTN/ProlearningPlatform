package com.cabybara.prolearningplatform.service.redis.impl;

import com.cabybara.prolearningplatform.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        redisTemplate.delete(keys);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean expire(String key, long timeoutInSeconds) {
        return redisTemplate.expire(key, timeoutInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Long getTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public void sAdd(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public void sRemove(String key, String value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    @Override
    public Set<String> sMembers(String key) {
        Set<Object> raw = redisTemplate.opsForSet().members(key);
        if (raw == null || raw.isEmpty()) return Collections.emptySet();
        return raw.stream().map(Object::toString).collect(Collectors.toSet());
    }
}
