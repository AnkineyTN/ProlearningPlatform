package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.Flashcard;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CardItemRepository extends JpaRepository<CardItem, Long> {
    @Modifying
    @Query("DELETE FROM CardItem ci " +
            "WHERE ci.id IN :cardIds " +
            "AND EXISTS (" +
            "  SELECT f FROM Flashcard f JOIN f.set s JOIN s.user u " +
            "  WHERE f.id = :flashcardId " +
            "  AND f.set.id = :setId " +
            "  AND s.user.id = :userId " +
            "  AND ci.flashcard = f" +
            ")")
    int deleteAllByIdInAndOwnershipChecks(
            @Param("cardIds") List<Long> cardIds,
            @Param("flashcardId") Long flashcardId,
            @Param("setId") Long setId,
            @Param("userId") Long userId
    );

    @EntityGraph(attributePaths = {"flashcard", "flashcard.set", "flashcard.user"})
    List<CardItem> findAllByIdIn(List<Long> ids);

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

    @Query("SELECT c.flashcard.user.id as userId, COUNT(c) as dueCount " +
            "FROM CardItem c " +
            "WHERE c.nextReviewAt <= :now " +
            "GROUP BY c.flashcard.user.id")
    List<UserDueStatDto> findUsersWithDueCards(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY c.id ASC")
    List<CardItem> findAllByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Query("SELECT c FROM CardItem c WHERE c.flashcard.id = :flashcardId ORDER BY RANDOM()")
    List<CardItem> findAllByFlashcardIdRandomOrder(@Param("flashcardId") Long flashcardId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CardItem c WHERE c.flashcard.id = :flashcardId")
    long countByFlashcardId(@Param("flashcardId") Long flashcardId);
}
