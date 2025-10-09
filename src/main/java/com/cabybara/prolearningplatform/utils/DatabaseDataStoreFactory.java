package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.repository.TokenDatabaseRepository;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;

import java.io.IOException;
import java.io.Serializable;

public class DatabaseDataStoreFactory extends AbstractDataStoreFactory {
    private TokenDatabaseRepository tokenDatabaseRepository;

    public DatabaseDataStoreFactory(TokenDatabaseRepository tokenDatabaseRepository) {
        this.tokenDatabaseRepository = tokenDatabaseRepository;
    }

    @Override
    protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
        return new DatabaseDataStore<>(this, id, tokenDatabaseRepository);
    }
}
