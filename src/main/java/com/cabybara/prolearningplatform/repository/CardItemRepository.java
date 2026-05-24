package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.UserDueCardProjection;
import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CardItemRepository extends JpaRepository<CardItem, Long> {
    @EntityGraph(attributePaths = {"flashcard", "flashcard.set", "flashcard.user"})
    List<CardItem> findAllByIdIn(List<Long> ids);

    @Query("SELECT ci FROM CardItem ci " +
           "LEFT JOIN FETCH ci.flashcard f " +
           "LEFT JOIN FETCH f.set s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH ci.image " +
           "WHERE ci.id IN :ids")
    List<CardItem> findAllWithImageByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId " +
            "AND c.nextReviewAt <= :now " +
            "ORDER BY c.intervalDays ASC")
    List<CardItem> findDueCards(@Param("flashcardId") Long flashcardId,
                                @Param("now") OffsetDateTime now,
                                Pageable pageable);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId " +
            "AND c.repetitions = 0" +
            "ORDER BY c.id ASC")
    List<CardItem> findNewCards(@Param("flashcardId") Long flashcardId, Pageable pageable);

    @Query("SELECT c.flashcard.user.id as userId, COUNT(c) as dueCount, c.flashcard.user.language as userLanguage " +
            "FROM CardItem c " +
            "WHERE c.nextReviewAt <= :now " +
            "GROUP BY c.flashcard.user.id, c.flashcard.user.language")
    List<UserDueStatDto> findUsersWithDueCards(@Param("now") OffsetDateTime now);

    @Query("SELECT c.flashcard.user.id AS userId, c.id AS cardId, c.flashcard.user.language AS userLanguage " +
            "FROM CardItem c " +
            "WHERE c.nextReviewAt <= :now AND c.flashcard.set IS NOT NULL")
    List<UserDueCardProjection> findDueCardsByUser(@Param("now") OffsetDateTime now);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY c.id ASC")
    List<CardItem> findAllByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY RANDOM()")
    List<CardItem> findAllByFlashcardIdRandomOrder(@Param("flashcardId") Long flashcardId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CardItem c WHERE c.flashcard.id = :flashcardId")
    long countByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId AND c.topic IS NULL")
    List<CardItem> findByFlashcardIdAndTopicIsNull(@Param("flashcardId") Long flashcardId);

    @Query(value = """
        WITH combined_topics AS (
            SELECT ci.topic,
                   'FLASHCARD' AS type,
                   f.id AS resource_id,
                   f.created_at
            FROM card_item ci
            INNER JOIN flashcard f ON ci.flashcard_id = f.id
            WHERE f.privacy = 'PUBLIC' AND ci.topic IS NOT NULL AND ci.topic <> ''
            
            UNION ALL
            
            SELECT q.topic,
                   'EXAM' AS type,
                   e.id AS resource_id,
                   e.created_at
            FROM questions q
            INNER JOIN exam_questions eq ON eq.question_id = q.id
            INNER JOIN exams e ON eq.exam_id = e.id
            WHERE e.privacy = 'PUBLIC' AND q.topic IS NOT NULL AND q.topic <> ''
        )
        SELECT topic,
               COUNT(DISTINCT CONCAT(type, '_', resource_id)) AS total_resources,
               COUNT(DISTINCT CASE WHEN created_at >= :since THEN CONCAT(type, '_', resource_id) END) AS new_resources
        FROM combined_topics
        GROUP BY topic
        ORDER BY total_resources DESC, new_resources DESC
    """, nativeQuery = true)
    List<Object[]> findTopTopics(@Param("since") OffsetDateTime since, org.springframework.data.domain.Pageable pageable);
}
