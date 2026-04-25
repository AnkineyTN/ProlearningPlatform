package com.cabybara.prolearningplatform.repository;

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

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY c.id ASC")
    List<CardItem> findAllByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY RANDOM()")
    List<CardItem> findAllByFlashcardIdRandomOrder(@Param("flashcardId") Long flashcardId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CardItem c WHERE c.flashcard.id = :flashcardId")
    long countByFlashcardId(@Param("flashcardId") Long flashcardId);
}
