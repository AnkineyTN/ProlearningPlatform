package com.cabybara.prolearningplatform.model;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "google_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCredential extends AbstractEntity {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "access_token", nullable = false, length = 1024)
    private String accessToken;

    @Column(name = "refresh_token", length = 1024)
    private String refreshToken;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
