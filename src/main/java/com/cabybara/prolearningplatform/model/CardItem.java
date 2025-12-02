package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_item")
public class CardItem extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    @JsonIgnore
    private Flashcard flashcard;

    @Column(name = "front_card", nullable = false, columnDefinition = "TEXT")
    private String frontCard;

    @Column(name = "back_card", nullable = false, columnDefinition = "TEXT")
    private String backCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    @Builder.Default
    private CardStatus cardStatus = CardStatus.NEW;

    @Column(name = "next_review_at", nullable = true)
    private OffsetDateTime nextReviewAt;

    @Column(name = "interval_days")
    @Builder.Default
    private Integer intervalDays = 0;

    @Column(name = "ease_factor")
    @Builder.Default
    private Float easeFactor = 2.5f;

    @Column(name = "repetitions")
    @Builder.Default
    private Integer repetitions = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset image;
}