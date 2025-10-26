package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Flashcard;
import com.cabybara.prolearningplatform.model.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    Optional<Flashcard> findByTitle(String title);

    boolean existsBySetIdAndTitle(Long userId, @NotEmpty @NotBlank @NotNull String title);

    Page<Flashcard> findAllBySetIdAndUserId(Long setId, Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cards", "cards.image"})
    Optional<Flashcard> findByIdAndSetIdAndUserId(Long flashcardId, Long setId, Long userId);
}
