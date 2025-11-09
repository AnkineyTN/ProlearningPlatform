package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.CardItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
