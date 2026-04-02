package com.cabybara.prolearningplatform.model.flashcard_study_session;

import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "study_session_review_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"session", "card"})
@EqualsAndHashCode(exclude = {"session", "card"})
public class StudySessionReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FlashcardStudySession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardItem card;

    @Column(name = "is_known", nullable = false)
    private boolean known;

    @Column(name = "reviewed_at", nullable = false)
    private OffsetDateTime reviewedAt;
}