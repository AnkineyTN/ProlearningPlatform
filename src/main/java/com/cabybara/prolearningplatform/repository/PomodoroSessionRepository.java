package com.cabybara.prolearningplatform.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.enums.PomodoroSessionType;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSession;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    // Lấy session theo khoảng thời gian (dùng cho weekly stats)
    @Query("""
        SELECT s FROM PomodoroSession s
        WHERE s.user.id = :userId
        AND s.startedAt >= :from
        AND s.startedAt < :to
        ORDER BY s.startedAt ASC
    """)
    List<PomodoroSession> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );

    // Lấy ngày có completed session (dùng tính streak)
    @Query(value = """
        SELECT DISTINCT DATE(started_at AT TIME ZONE :timezone)
        FROM pomodoro_session
        WHERE id_user = :userId
        AND completed = true
        AND type = 'POMODORO'
        ORDER BY 1 DESC
        LIMIT 60
    """, nativeQuery = true)
    List<java.sql.Date> findRecentCompletedDays(
        @Param("userId") Long userId,
        @Param("timezone") String timezone
    );

    // Tổng số session trong ngày hôm nay
    @Query("""
        SELECT COUNT(s) FROM PomodoroSession s
        WHERE s.user.id = :userId
        AND s.type = :type
        AND s.completed = true
        AND s.startedAt >= :startOfDay
        AND s.startedAt < :endOfDay
    """)
    long countCompletedToday(
        @Param("userId") Long userId,
        @Param("type") PomodoroSessionType type,
        @Param("startOfDay") OffsetDateTime startOfDay,
        @Param("endOfDay") OffsetDateTime endOfDay
    );

    long countByUser_Id(Long userId);
}