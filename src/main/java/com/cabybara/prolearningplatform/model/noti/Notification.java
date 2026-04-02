package com.cabybara.prolearningplatform.model.noti;

import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> data;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "push_sent", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean pushSent = false;
}

