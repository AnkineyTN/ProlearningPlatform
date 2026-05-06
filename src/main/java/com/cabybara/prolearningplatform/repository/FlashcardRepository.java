package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    Optional<Flashcard> findByTitle(String title);

    boolean existsBySetIdAndTitle(Long userId, @NotEmpty @NotBlank @NotNull String title);

    Page<Flashcard> findAllBySetIdAndUserId(Long setId, Long userId, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM flashcard
                WHERE id_user = :userId AND id_set = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
            """,
            nativeQuery = true)
    Page<Flashcard> findByUserIdAndSetId(Long userId, Long setId, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM flashcard
                WHERE id_user = :userId AND id_set = :setId AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
            """,
            nativeQuery = true)
    Page<Flashcard> findByUserIdAndSetIdAndPrivacy(Long userId, Long setId, String privacy, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM flashcard
                WHERE id_user = :userId AND id_set = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM flashcard
                WHERE id_user = :userId AND id_set = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Flashcard> searchByUserIdAndSetId(Long userId, Long setId, String q, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM flashcard
                WHERE id_user = :userId
                  AND id_set = :setId
                  AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM flashcard
                WHERE id_user = :userId
                  AND id_set = :setId
                  AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Flashcard> searchByUserIdAndSetIdAndPrivacy(Long userId, Long setId, String q, String privacy, String createMethod, Pageable pageable);

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

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Flashcard f " +
            "WHERE f.set.id = :setId " +
            "AND f.id = :flashcardId " +
            "AND f.set.user.id = :userId")
    boolean existsBySetIdAndIdAndUserId(@Param("setId") Long setId,
                                        @Param("flashcardId") Long flashcardId,
                                        @Param("userId") Long userId);

    @Query("SELECT f.privacy FROM Flashcard f WHERE f.id = :flashcardId")
    Optional<Privacy> findPrivacyById(@Param("flashcardId") Long flashcardId);

    @EntityGraph(attributePaths = {"cards", "cards.image", "set"})
    Optional<Flashcard> findByIdAndSetId(Long flashcardId, Long setId);

    @Query("SELECT f.id FROM Flashcard f WHERE f.set.id = :setId")
    List<Long> findIdsBySetId(@Param("setId") Long setId);

    @Modifying
    @Query("UPDATE Flashcard f SET f.updatedAt = :now WHERE f.id = :id")
    void updateUpdatedAt(@Param("id") Long id, @Param("now") OffsetDateTime now);
}
