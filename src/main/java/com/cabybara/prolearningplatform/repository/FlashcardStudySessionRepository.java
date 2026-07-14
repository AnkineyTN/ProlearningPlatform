package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardStudySessionRepository extends JpaRepository<FlashcardStudySession, Long> {
    Optional<FlashcardStudySession> findByUserIdAndFlashcardIdAndStatus(Long userId, Long flashcardId, FlashcardStudySessionStatus status);

    List<FlashcardStudySession> findByUserIdAndSetIdAndFlashcardIdOrderById (
            Long userId,
            Long setId,
            Long flashcardId
    );

    Long user(User user);

    List<FlashcardStudySession> findByUserIdAndSetIdAndFlashcardIdAndStatus(Long userId, Long setId, Long flashcardId, FlashcardStudySessionStatus flashcardStudySessionStatus);

    Optional<FlashcardStudySession> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT s FROM FlashcardStudySession s " +
           "WHERE s.user.id = :userId AND s.flashcard.id = :flashcardId AND s.status = :status " +
           "ORDER BY s.lastInteractionAt DESC LIMIT 1")
    Optional<FlashcardStudySession> findLatestSessionByStatus(
            @Param("userId") Long userId,
            @Param("flashcardId") Long flashcardId,
            @Param("status") FlashcardStudySessionStatus status
    );

    @Query("SELECT r.card.id FROM StudySessionReviewLog r WHERE r.session.id = :sessionId AND r.known = false")
    List<Long> findIncorrectCardIdsBySessionId(@Param("sessionId") Long sessionId);
}
