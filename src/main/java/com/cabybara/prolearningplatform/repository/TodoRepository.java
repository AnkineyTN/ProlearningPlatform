package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.UserTodoCountProjection;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
            SELECT t FROM Todo t
            WHERE t.user.id = :userId
              AND (:goalId IS NULL OR t.goal.id = :goalId)
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:noGoal IS NULL OR (:noGoal = true AND t.goal IS NULL) OR (:noGoal = false AND t.goal IS NOT NULL))
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
            """)
    Page<Todo> findByFilters(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId,
            @Param("completed") Boolean completed,
            @Param("priority") TodoPriority priority,
            @Param("noGoal") Boolean noGoal,
            @Param("type") TodoType type,
            @Param("status") TodoStatus status,
            Pageable pageable
    );

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    long countByGoalId(Long goalId);

    long countByGoalIdAndCompleted(Long goalId, Boolean completed);

    @Query("""
            SELECT t.user.id AS userId, COUNT(t) AS count, t.user.language AS userLanguage
            FROM Todo t
            WHERE t.user.id IN :userIds
              AND t.type = com.cabybara.prolearningplatform.enums.TodoType.DAILY
              AND t.status = com.cabybara.prolearningplatform.enums.TodoStatus.TODO
              AND t.dueDate = :today
            GROUP BY t.user.id, t.user.language
            """)
    List<UserTodoCountProjection> countIncompleteDailyTodosForUsers(
            @Param("userIds") List<Long> userIds,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT t.user.id AS userId, COUNT(t) AS count, t.user.language AS userLanguage
            FROM Todo t
            WHERE t.user.id IN :userIds
              AND t.type = com.cabybara.prolearningplatform.enums.TodoType.WEEKLY
              AND t.status = com.cabybara.prolearningplatform.enums.TodoStatus.TODO
              AND t.dueDate BETWEEN :weekStart AND :weekEnd
            GROUP BY t.user.id, t.user.language
            """)
    List<UserTodoCountProjection> countIncompleteWeeklyTodosForUsers(
            @Param("userIds") List<Long> userIds,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );
}
