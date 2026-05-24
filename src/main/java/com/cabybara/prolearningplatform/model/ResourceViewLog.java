package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "resource_view_log", indexes = {
        @Index(name = "idx_rvl_resource", columnList = "resource_id, resource_type"),
        @Index(name = "idx_rvl_viewed_at", columnList = "viewed_at"),
        @Index(name = "idx_rvl_resource_time", columnList = "resource_id, resource_type, viewed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ContentType resourceType;

    @Column(name = "viewer_ip", length = 45)
    private String viewerIp;

    @Column(name = "user_id")
    private Long userId;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private OffsetDateTime viewedAt;
}
