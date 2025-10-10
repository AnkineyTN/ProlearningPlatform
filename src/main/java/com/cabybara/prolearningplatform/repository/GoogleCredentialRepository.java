package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.GoogleCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleCredentialRepository extends JpaRepository<GoogleCredential, Long> {
    boolean existsByAccessTokenAndRefreshToken(String accessToken, String refreshToken);

    Optional<GoogleCredential> findByUserId(String key);

    boolean existsByUserId(String id);

    void deleteByUserId(String userId);
}
