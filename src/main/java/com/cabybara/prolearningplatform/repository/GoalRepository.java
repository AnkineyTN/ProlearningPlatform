package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import com.cabybara.prolearningplatform.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    @Query("""
            SELECT g FROM Goal g
            WHERE g.user.id = :userId
              AND (:status IS NULL OR g.status = :status)
              AND (:type IS NULL OR g.type = :type)
            """)
    Page<Goal> findByFilters(
            @Param("userId") Long userId,
            @Param("status") GoalStatus status,
            @Param("type") GoalType type,
            Pageable pageable
    );

    Page<Goal> findByUserId(Long userId, Pageable pageable);

    Page<Goal> findByUserIdAndStatus(Long userId, GoalStatus status, Pageable pageable);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    List<Goal> findByParentGoalIdAndUserId(Long parentGoalId, Long userId);

    @Query("""
            SELECT g FROM Goal g
            JOIN FETCH g.user
            WHERE g.user.id IN :userIds
              AND g.status = com.cabybara.prolearningplatform.enums.GoalStatus.IN_PROGRESS
              AND g.targetDate = :targetDate
            """)
    List<Goal> findGoalsWithTargetDate(
            @Param("userIds") List<Long> userIds,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("""
            SELECT DISTINCT g FROM Goal g
            JOIN FETCH g.user
            WHERE g.user.id IN :userIds
              AND g.status = com.cabybara.prolearningplatform.enums.GoalStatus.IN_PROGRESS
              AND g.targetDate IS NOT NULL
              AND g.updatedAt < :cutoffTime
              AND NOT EXISTS (
                  SELECT 1 FROM Todo t
                  WHERE t.goal = g
                  AND t.updatedAt >= :cutoffTime
              )
            """)
    List<Goal> findInactiveGoals(
            @Param("userIds") List<Long> userIds,
            @Param("cutoffTime") OffsetDateTime cutoffTime
    );
}
