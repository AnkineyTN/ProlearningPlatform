package com.cabybara.prolearningplatform.repository;

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
                   SUM(a.active_duration)  AS total_active_seconds,
                   COUNT(*)               AS sessions,
                   MAX(a.score)           AS best_score,
                   SUM(a.items_count)     AS total_items
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
              AND a.date <= :endDate
            GROUP BY a.date
            ORDER BY a.date
            """, nativeQuery = true)
    List<Object[]> findHeatmapData(
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
    List<LocalDate> findActiveDays(@Param("userId") Long userId);

    @Query(value = """
            SELECT a.content_type,
                   SUM(a.active_duration) AS total_active_seconds,
                   COUNT(*)              AS sessions
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
            GROUP BY a.content_type
            """, nativeQuery = true)
    List<Object[]> findSummaryByContentType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT SUM(a.active_duration), COUNT(*), SUM(a.items_count),
                   AVG(CASE WHEN a.score IS NOT NULL THEN a.score END)
            FROM activity_log a
            WHERE a.user_id = :userId
              AND a.date >= :startDate
            """, nativeQuery = true)
    List<Object[]> findOverallSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);
}
