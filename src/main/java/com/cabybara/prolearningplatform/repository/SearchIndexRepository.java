package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.model.SearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {
    @Query(value = """
    SELECT
        entity_id AS id,
        set_id AS setId,
        entity_type AS type,
        title,
        description,
        ts_rank(search_vector, query) * 0.7 +
        similarity(title, :keyword) * 0.3 AS score,
        user_id as userId
    FROM search_index,
         websearch_to_tsquery('simple', unaccent(:keyword)) query
    WHERE
        (
            search_vector @@ query
            OR (title || ' ' || description) % unaccent(:keyword)
        )
        AND (:userId IS NULL OR user_id = :userId)
        AND (:type IS NULL OR entity_type = :type)
    ORDER BY score DESC
    """,
    countQuery = """
    SELECT count(*)
    FROM search_index,
         websearch_to_tsquery('simple', unaccent(:keyword)) query
    WHERE
        (
            search_vector @@ query
            OR (title || ' ' || description) % unaccent(:keyword)
        )
        AND (:userId IS NULL OR user_id = :userId)
        AND (:type IS NULL OR entity_type = :type)
    """,
    nativeQuery = true)
    Page<SearchResultDto> search(
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            @Param("type") String type,
            Pageable pageable
    );
}
