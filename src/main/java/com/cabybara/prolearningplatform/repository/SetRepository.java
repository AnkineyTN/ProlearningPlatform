package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.RoadmapSetRef;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Set;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
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

    boolean existsByTitleAndUserId(String title, Long userId);

    Page<Set> findAllByUserId(Long userId, Pageable pageable);

    Optional<Set> findByIdAndUserId(Long setId, Long userId);

    Optional<Set> findByRoadmapId(Long roadmapId);

    @Query("SELECT s.roadmap.id AS roadmapId, s.id AS setId FROM Set s WHERE s.roadmap.id IN :roadmapIds")
    List<RoadmapSetRef> findSetRefsByRoadmapIdIn(@Param("roadmapIds") Collection<Long> roadmapIds);

    boolean existsByTitleAndIdNot(String title, Long id);

    @Modifying
    @Query("UPDATE Set s SET s.updatedAt = :now WHERE s.id = :id")
    void updateLastModifiedDate(@Param("id") Long id, @Param("now") OffsetDateTime now);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
            """,
            nativeQuery = true)
    Page<Set> findByUserId(Long userId, Boolean roadmap, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId AND privacy = :privacy
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
            """,
            nativeQuery = true)
    Page<Set> findByUserIdAndPrivacy(Long userId, String privacy, Boolean roadmap, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
                  AND (search_vector @@ websearch_to_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM set
                WHERE id_user = :userId
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
                  AND (
                        search_vector @@ websearch_to_tsquery('simple', unaccent(:q))
                        OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Set> searchByUserId(Long userId, String q, Boolean roadmap, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM set
                WHERE id_user = :userId
                  AND privacy = :privacy
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
                  AND (search_vector @@ websearch_to_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM set
                WHERE id_user = :userId
                  AND privacy = :privacy
                  AND (CAST(:roadmap AS BOOLEAN) IS NULL OR (roadmap_id IS NOT NULL) = CAST(:roadmap AS BOOLEAN))
                  AND (search_vector @@ websearch_to_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Set> searchByUserIdAndPrivacy(Long userId, String q, String privacy, Boolean roadmap, Pageable pageable);
}
