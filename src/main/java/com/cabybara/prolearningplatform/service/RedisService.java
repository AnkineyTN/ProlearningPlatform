package com.cabybara.prolearningplatform.service;

public interface RedisService {
    void set(String key, Object value, long timeoutInSeconds);

    void set(String key, Object value);

    Object get(String key);

    void delete(String key);

    boolean hasKey(String key);

    // set ttl for key
    boolean expire(String key, long timeoutInSeconds);

    Long getTTL(String key);
}
