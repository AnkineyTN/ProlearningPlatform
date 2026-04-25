package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.FlashcardGameRankingEntry;
import com.cabybara.prolearningplatform.model.flashcard.FlashcardGameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardGameHistoryRepository extends JpaRepository<FlashcardGameHistory, Long> {

    List<FlashcardGameHistory> findByFlashcardIdAndUserIdOrderByCompletedAtDesc(Long flashcardId, Long userId);

    @Query(nativeQuery = true, value = """
            SELECT u.id          AS userId,
                   u.first_name  AS firstName,
                   u.last_name   AS lastName,
                   MIN(h.duration_seconds) AS bestDuration,
                   COUNT(h.id)   AS playCount
            FROM flashcard_game_history h
            JOIN users u ON u.id = h.user_id
            WHERE h.flashcard_id = :flashcardId
            GROUP BY u.id, u.first_name, u.last_name
            ORDER BY bestDuration ASC
            LIMIT 20
            """)
    List<FlashcardGameRankingEntry> findRankingByFlashcardId(@Param("flashcardId") Long flashcardId);
}
