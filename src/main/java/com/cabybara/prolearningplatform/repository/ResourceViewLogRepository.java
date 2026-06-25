package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.ResourceViewCountProjection;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.ResourceViewLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ResourceViewLogRepository extends JpaRepository<ResourceViewLog, Long> {

    boolean existsByResourceIdAndResourceTypeAndViewerIpAndViewedAtAfter(
            Long resourceId, ContentType resourceType, String viewerIp, OffsetDateTime since);

    @Query(value = """
        SELECT r.resource_id AS resourceId, r.resource_type AS resourceType, COUNT(r.id) AS viewCount
        FROM resource_view_log r
        LEFT JOIN note n ON r.resource_id = n.id AND CAST(r.resource_type AS varchar) = 'NOTE'
        LEFT JOIN flashcard f ON r.resource_id = f.id AND CAST(r.resource_type AS varchar) = 'FLASHCARD'
        LEFT JOIN exams e ON r.resource_id = e.id AND CAST(r.resource_type AS varchar) = 'EXAM'
        WHERE r.viewed_at >= :since
          AND (CAST(n.privacy AS varchar) = 'PUBLIC' OR CAST(f.privacy AS varchar) = 'PUBLIC' OR CAST(e.privacy AS varchar) = 'PUBLIC')
        GROUP BY r.resource_id, r.resource_type
        ORDER BY viewCount DESC
        """, nativeQuery = true)
    List<ResourceViewCountProjection> findTopResourcesByViews(
            @Param("since") OffsetDateTime since,
            Pageable pageable);

    @Query(value = """
        SELECT r.resource_id AS resourceId, r.resource_type AS resourceType, COUNT(r.id) AS viewCount
        FROM resource_view_log r
        LEFT JOIN note n ON r.resource_id = n.id AND CAST(r.resource_type AS varchar) = 'NOTE'
        LEFT JOIN flashcard f ON r.resource_id = f.id AND CAST(r.resource_type AS varchar) = 'FLASHCARD'
        LEFT JOIN exams e ON r.resource_id = e.id AND CAST(r.resource_type AS varchar) = 'EXAM'
        WHERE CAST(r.resource_type AS varchar) = :#{#resourceType.name()} AND r.viewed_at >= :since
          AND (CAST(n.privacy AS varchar) = 'PUBLIC' OR CAST(f.privacy AS varchar) = 'PUBLIC' OR CAST(e.privacy AS varchar) = 'PUBLIC')
        GROUP BY r.resource_id, r.resource_type
        ORDER BY viewCount DESC
        """, nativeQuery = true)
    List<ResourceViewCountProjection> findTopResourcesByViewsAndType(
            @Param("resourceType") ContentType resourceType,
            @Param("since") OffsetDateTime since,
            Pageable pageable);
}
