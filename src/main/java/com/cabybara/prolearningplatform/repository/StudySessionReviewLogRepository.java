package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.IncorrectCardsByFlashcard;
import com.cabybara.prolearningplatform.model.flashcard_study_session.StudySessionReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface StudySessionReviewLogRepository
        extends JpaRepository<StudySessionReviewLog, Long> {

    long countBySessionId(Long sessionId);

    List<StudySessionReviewLog> findBySessionIdOrderByReviewedAtAsc(Long sessionId);

    @Query("""
        SELECT DISTINCT rl.card.id
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        """)
    List<Long> findDistinctIncorrectCardIds(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
        SELECT DISTINCT rl.card.id
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.session.set.id = :setId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        """)
    List<Long> findDistinctIncorrectCardIdsBySet(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
        SELECT rl.card.flashcard.id AS flashcardId, COUNT(DISTINCT rl.card.id) AS incorrectCount
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        GROUP BY rl.card.flashcard.id
        """)
    List<IncorrectCardsByFlashcard> findIncorrectCountGroupByFlashcard(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
        SELECT rl.card.flashcard.id AS flashcardId, COUNT(DISTINCT rl.card.id) AS incorrectCount
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.session.set.id = :setId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        GROUP BY rl.card.flashcard.id
        """)
    List<IncorrectCardsByFlashcard> findIncorrectCountGroupByFlashcardAndSet(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
        SELECT COUNT(DISTINCT rl.card.id)
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        """)
    long countDistinctIncorrectCards(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("""
        SELECT COUNT(DISTINCT rl.card.id)
        FROM StudySessionReviewLog rl
        WHERE rl.session.user.id = :userId
          AND rl.session.set.id = :setId
          AND rl.known = false
          AND rl.reviewedAt BETWEEN :from AND :to
        """)
    long countDistinctIncorrectCardsBySet(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}