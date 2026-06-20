package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.ActiveDayProjection;
import com.cabybara.prolearningplatform.dto.helper.ContentTypeSummaryProjection;
import com.cabybara.prolearningplatform.dto.helper.HeatmapProjection;
import com.cabybara.prolearningplatform.dto.helper.OverallSummaryProjection;
import com.cabybara.prolearningplatform.dto.helper.ResourceSessionCountProjection;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Optional<ActivityLog> findByUserIdAndDateAndContentTypeAndSetId(
            Long userId, LocalDate date, ContentType contentType, Long setId);

    @Query(value = """
            SELECT a.date,
                   SUM(a.active_duration)  AS totalActiveSeconds,
                   COUNT(*)               AS sessions,
                   MAX(a.score)           AS bestScore,
                   SUM(a.items_count)     AS totalItems
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
              AND a.date <= :endDate
            GROUP BY a.date
            ORDER BY a.date
            """, nativeQuery = true)
    List<HeatmapProjection> findHeatmapData(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT a.date
            FROM activity_log a
            WHERE a.user_id = :userId
            GROUP BY a.date
            HAVING SUM(a.active_duration) >= 300
            ORDER BY a.date DESC
            """, nativeQuery = true)
    List<ActiveDayProjection> findActiveDays(@Param("userId") Long userId);

    @Query(value = """
            SELECT a.content_type AS contentType,
                   SUM(a.active_duration) AS totalActiveSeconds,
                   COUNT(*)              AS sessions
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
            GROUP BY a.content_type
            """, nativeQuery = true)
    List<ContentTypeSummaryProjection> findSummaryByContentType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT SUM(a.active_duration) AS totalActiveSeconds,
                   COUNT(*)              AS totalSessions,
                   SUM(a.items_count)    AS totalItems,
                   AVG(CASE WHEN a.score IS NOT NULL THEN a.score END) AS avgScore
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
            """, nativeQuery = true)
    List<OverallSummaryProjection> findOverallSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    @Query(value = """
        SELECT a.set_id AS setId, a.content_type AS contentType, COUNT(a.id) AS sessionCount
        FROM activity_log a
        LEFT JOIN note n ON a.set_id = n.id AND CAST(a.content_type AS varchar) = 'NOTE'
        LEFT JOIN flashcard f ON a.set_id = f.id AND CAST(a.content_type AS varchar) = 'FLASHCARD'
        LEFT JOIN exams e ON a.set_id = e.id AND CAST(a.content_type AS varchar) = 'EXAM'
        WHERE a.set_id IS NOT NULL 
          AND CAST(a.content_type AS varchar) IN (:allowedTypesStr)
          AND a.date >= :startDate
          AND (CAST(n.privacy AS varchar) = 'PUBLIC' OR CAST(f.privacy AS varchar) = 'PUBLIC' OR CAST(e.privacy AS varchar) = 'PUBLIC')
        GROUP BY a.set_id, a.content_type
        ORDER BY sessionCount DESC
        """, nativeQuery = true)
    List<ResourceSessionCountProjection> findTopResourcesBySessions(
            @Param("startDate") LocalDate startDate,
            @Param("allowedTypesStr") List<String> allowedTypesStr,
            org.springframework.data.domain.Pageable pageable);
}
