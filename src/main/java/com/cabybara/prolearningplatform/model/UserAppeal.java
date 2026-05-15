package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.AppealStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_appeal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAppeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @ToString.Exclude
    private User user;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private AppealStatus status = AppealStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;
}
