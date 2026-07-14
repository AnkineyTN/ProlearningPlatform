package com.cabybara.prolearningplatform.repository.impl;

import com.cabybara.prolearningplatform.mapper.GoogleAuthItemMapper;
import com.cabybara.prolearningplatform.model.GoogleCredential;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.GoogleCredentialRepository;
import com.cabybara.prolearningplatform.repository.TokenDatabaseRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TokenDatabaseRepositoryImpl implements TokenDatabaseRepository<StoredCredential> {

    private final UserRepository userRepository;
    private final GoogleCredentialRepository googleCredentialRepository;
    private final GoogleAuthItemMapper googleAuthItemMapper;

    @Override
    public int size() {
        return Math.toIntExact(googleCredentialRepository.count());
    }

    @Override
    public boolean isEmpty() {
        return Math.toIntExact(googleCredentialRepository.count()) == 0;
    }

    @Override
    public boolean containsKey(String key) {
        return googleCredentialRepository.existsByUserId(key);
    }

    @Override
    public boolean containsValue(StoredCredential value) {
        GoogleCredential googleCredential = googleAuthItemMapper.fromStoreCredential(value);
        return googleCredentialRepository.existsByAccessTokenAndRefreshToken(googleCredential.getAccessToken(), googleCredential.getRefreshToken());
    }

    @Override
    public Set<String> keySet() {
        return googleCredentialRepository.findAll().stream()
                .map(GoogleCredential::getUserId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<StoredCredential> values() {
        return googleCredentialRepository.findAll().stream()
                .map(googleAuthItemMapper::toStoreCredential)
                .toList();
    }

    @Override
    public StoredCredential get(String key) {
        Optional<GoogleCredential> optionalGoogleAuthItem = googleCredentialRepository.findByUserId(key);
        return optionalGoogleAuthItem.map(googleAuthItemMapper::toStoreCredential).orElse(null);
    }

    @Override
    @Transactional
    public TokenDatabaseRepository<StoredCredential> set(String key, StoredCredential value) {
        GoogleCredential googleAuthItem = googleCredentialRepository.findByUserId(key)
                .map(existing -> {
                    existing.setAccessToken(value.getAccessToken());
                    existing.setRefreshToken(value.getRefreshToken());
                    if (value.getExpirationTimeMilliseconds() != null) {
                        existing.setExpiresAt(Instant.ofEpochMilli(value.getExpirationTimeMilliseconds())
                                .atOffset(ZoneOffset.UTC));
                    } else {
                        existing.setExpiresAt(null);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    GoogleCredential newItem = googleAuthItemMapper.fromStoreCredential(value);
                    newItem.setUserId(key);
                    return newItem;
                });
        googleCredentialRepository.save(googleAuthItem);
        return this;
    }

    @Override
    @Transactional
    public TokenDatabaseRepository<StoredCredential> clear() {
        googleCredentialRepository.deleteAll();
        return this;
    }

    @Override
    @Transactional
    public void delete(String key) {
        googleCredentialRepository.deleteByUserId(key);
    }
}
