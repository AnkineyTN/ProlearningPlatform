package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.model.SearchIndex;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {

    @Query(value = """
        SELECT
            entity_id AS id,
            entity_type AS type,
            title,
            description,
            ts_rank(search_vector, query) * 0.7 +
            similarity(title, :keyword) * 0.3 AS score,
            user_id as userId
        FROM search_index,
             websearch_to_tsquery('simple', unaccent(:keyword)) query
        WHERE
            search_vector @@ query
           OR (title || ' ' || description) % unaccent(:keyword)
        ORDER BY score DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<SearchResultDto> searchAll(@Param("keyword") String keyword, @Param("limit") int limit);

    @Query(value = """
        SELECT
            entity_id AS id,
            title,
            description,
            entity_type AS type,
            ts_rank(search_vector, query) * 0.7 +
            similarity(title, :keyword) * 0.3 AS score,
            user_id as userId
        FROM search_index,
             websearch_to_tsquery('simple', unaccent(:keyword)) query
        WHERE
            user_id = :userId
            AND (
            search_vector @@ query
           OR (title || ' ' || description) % unaccent(:keyword))
        ORDER BY score DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<SearchResultDto> searchByUserId(@Param("keyword") String keyword, @Param("userId") Long userId, @Param("limit") int limit);
}