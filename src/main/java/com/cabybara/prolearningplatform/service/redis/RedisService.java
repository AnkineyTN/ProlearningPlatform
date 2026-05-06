package com.cabybara.prolearningplatform.service.redis;

import java.util.Collection;
import java.util.Set;

public interface RedisService {
    void set(String key, Object value, long timeoutInSeconds);

    void set(String key, Object value);

    Object get(String key);

    void delete(String key);

    void delete(Collection<String> keys);

    boolean hasKey(String key);

    // set ttl for key
    boolean expire(String key, long timeoutInSeconds);

    Long getTTL(String key);

    void sAdd(String key, String value);

    void sRemove(String key, String value);

    Set<String> sMembers(String key);
}
