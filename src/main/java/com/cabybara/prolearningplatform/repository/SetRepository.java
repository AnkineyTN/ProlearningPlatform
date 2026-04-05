package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Set;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<Set, Long> {
    Optional<Set> findByTitle(String title);

    boolean existsByTitle(String title);

    Page<Set> findAllByUserId(Long userId, Pageable pageable);

    Optional<Set> findByIdAndUserId(Long setId, Long userId);

    boolean existsByTitleAndIdNot(String title, Long id);

    @Modifying
    @Query("UPDATE Set s SET s.updatedAt = :now WHERE s.id = :id")
    void updateLastModifiedDate(@Param("id") Long id, @Param("now") OffsetDateTime now);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
            """,
            nativeQuery = true)
    Page<Set> findByUserId(Long userId, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId AND privacy = :privacy
            """,
            nativeQuery = true)
    Page<Set> findByUserIdAndPrivacy(Long userId, String privacy, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM set
                WHERE id_user = :userId
                  AND (
                        search_vector @@ plainto_tsquery('simple', :q)
                        OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Set> searchByUserId(Long userId, String q, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Set> searchByUserIdAndPrivacy(Long userId, String q, String privacy, Pageable pageable);
}
