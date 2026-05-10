package com.cabybara.prolearningplatform.model.flashcard;

import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "flashcard_game_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardGameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_cards", nullable = false)
    private Integer totalCards;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private OffsetDateTime completedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "flashcard_game_wrong_card",
            joinColumns = @JoinColumn(name = "history_id")
    )
    @MapKeyColumn(name = "card_item_id")
    @Column(name = "wrong_count")
    @Builder.Default
    private Map<Long, Integer> wrongCardCounts = new HashMap<>();
}
