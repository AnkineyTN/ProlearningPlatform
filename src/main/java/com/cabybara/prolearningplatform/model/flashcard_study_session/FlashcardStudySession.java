package com.cabybara.prolearningplatform.model.flashcard_study_session;

import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.enums.StudyMode;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "study_session")
public class FlashcardStudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("'IN_PROGRESS'")
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FlashcardStudySessionStatus status = FlashcardStudySessionStatus.IN_PROGRESS;

    @ColumnDefault("'SPACED_REPETITION'")
    @Column(name = "study_mode", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StudyMode studyMode = StudyMode.SPACED_REPETITION;

    @Column(name = "initial_card_ids")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> initialCardIds = new ArrayList<>();

    @Column(name = "remaining_card_ids")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> remainingCardIds = new ArrayList<>();

//    @ColumnDefault("'[]'")
//    @Column(name = "review_log")
//    @JdbcTypeCode(SqlTypes.JSON)
//    private List<FlashcardStudySessionLogItem> reviewLog = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("reviewedAt ASC")
    private List<StudySessionReviewLog> reviewLogs = new ArrayList<>();

    @ColumnDefault("0")
    @Column(name = "correct_count")
    private Integer correctCount;

    @ColumnDefault("0")
    @Column(name = "incorrect_count")
    private Integer incorrectCount;

    @ColumnDefault("now()")
    @Column(name = "last_interaction_at")
    private Instant lastInteractionAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "set_id", nullable = false)
    private Set set;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    public void addReviewLog(StudySessionReviewLog log) {
        reviewLogs.add(log);
        log.setSession(this);
    }
}