package com.cabybara.prolearningplatform.repository;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

public interface TokenDatabaseRepository<V> {
    int size();

    boolean isEmpty();

    boolean containsKey(String key);

    boolean containsValue(V value);

    Set<String> keySet();

    Collection<V> values();

    V get(String key);

    TokenDatabaseRepository<V> set(String key, V value);

    TokenDatabaseRepository<V> clear();

    void delete(String key);
}
