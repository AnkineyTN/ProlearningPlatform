package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Flashcard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    Optional<Flashcard> findByTitle(String title);

    boolean existsBySetIdAndTitle(Long userId, @NotEmpty @NotBlank @NotNull String title);

    Page<Flashcard> findAllBySetIdAndUserId(Long setId, Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cards", "cards.image"})
    Optional<Flashcard> findByIdAndSetIdAndUserId(Long flashcardId, Long setId, Long userId);

    @Modifying
    @Query("DELETE FROM Flashcard f WHERE f.id = :flashcardId AND f.set.id = :setId AND f.set.user.id = :userId")
    int deleteByIdAndSetIdAndSetUserId(@Param("flashcardId") Long flashcardId,
                                       @Param("setId") Long setId,
                                       @Param("userId") Long userId);

    Optional<Flashcard> getFlashcardByIdAndSetIdAndSetUserId(
            Long flashcardId,
            Long setId,
            Long userId
    );
}
