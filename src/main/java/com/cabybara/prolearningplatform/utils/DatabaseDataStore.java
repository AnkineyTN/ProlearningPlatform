package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.repository.TokenDatabaseRepository;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class DatabaseDataStore<V extends Serializable> extends AbstractDataStore<V> {
    private final TokenDatabaseRepository<V> tokenDatabaseRepository;

    protected DatabaseDataStore(DataStoreFactory dataStoreFactory, String id, TokenDatabaseRepository<V> tokenDatabaseRepository) {
        super(dataStoreFactory, id);
        this.tokenDatabaseRepository = tokenDatabaseRepository;
    }

    @Override
    public int size() throws IOException {
        return tokenDatabaseRepository.size();
    }

    @Override
    public boolean isEmpty() throws IOException {
        return tokenDatabaseRepository.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return tokenDatabaseRepository.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return tokenDatabaseRepository.containsValue(value);
    }

    @Override
    public Set<String> keySet() throws IOException {
        return tokenDatabaseRepository.keySet();
    }

    @Override
    public Collection<V> values() throws IOException {
        return tokenDatabaseRepository.values();
    }

    @Override
    public V get(String key) throws IOException {
        return tokenDatabaseRepository.get(key);
    }

    @Override
    public DataStore<V> set(String key, V value) throws IOException {
        tokenDatabaseRepository.set(key, value);
        return this;
    }

    @Override
    public DataStore<V> clear() {
        tokenDatabaseRepository.clear();
        return this;
    }

    @Override
    public DataStore<V> delete(String key) throws IOException {
        tokenDatabaseRepository.delete(key);
        return this;
    }
}