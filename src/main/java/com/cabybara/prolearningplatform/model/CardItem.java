package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_asset_id")
    private ImageAsset image;
}