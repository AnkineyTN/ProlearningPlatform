package com.cabybara.prolearningplatform.repository;

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

    @Query("""
        SELECT r.resourceId, r.resourceType, COUNT(r) as viewCount
        FROM ResourceViewLog r
        WHERE r.viewedAt >= :since
        GROUP BY r.resourceId, r.resourceType
        ORDER BY viewCount DESC
        """)
    List<Object[]> findTopResourcesByViews(
            @Param("since") OffsetDateTime since,
            Pageable pageable);

    @Query("""
        SELECT r.resourceId, r.resourceType, COUNT(r) as viewCount
        FROM ResourceViewLog r
        WHERE r.resourceType = :resourceType AND r.viewedAt >= :since
        GROUP BY r.resourceId, r.resourceType
        ORDER BY viewCount DESC
        """)
    List<Object[]> findTopResourcesByViewsAndType(
            @Param("resourceType") ContentType resourceType,
            @Param("since") OffsetDateTime since,
            Pageable pageable);
}
