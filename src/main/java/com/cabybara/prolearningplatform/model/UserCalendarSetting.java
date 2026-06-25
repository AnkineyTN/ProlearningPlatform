package com.cabybara.prolearningplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_calendar_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCalendarSetting extends AbstractEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "calendar_sync_enabled", nullable = false)
    @Builder.Default
    private Boolean calendarSyncEnabled = false;

    @Column(name = "refresh_token", length = 2048)
    private String refreshToken;

    @Column(name = "access_token", length = 2048)
    private String accessToken;

    @Column(name = "token_expires_at")
    private OffsetDateTime tokenExpiresAt;
}
