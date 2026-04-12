package com.cabybara.prolearningplatform.model.review;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_bundles")
@Getter
@Setter
public class ReviewBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "set_id")
    private Long setId;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "card_ids", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> cardIds = new ArrayList<>();

    @Column(name = "period_from", nullable = false)
    private OffsetDateTime periodFrom;

    @Column(name = "period_to", nullable = false)
    private OffsetDateTime periodTo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
