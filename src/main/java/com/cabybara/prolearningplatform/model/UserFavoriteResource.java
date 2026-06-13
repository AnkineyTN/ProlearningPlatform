package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_favorite_resources", indexes = {
        @Index(name = "idx_favorite_user_resource", columnList = "user_id, resource_type, resource_id", unique = true),
        @Index(name = "idx_favorite_created_at", columnList = "created_at")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "resource_id", "resource_type"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ContentType resourceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
