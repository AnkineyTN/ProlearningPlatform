package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            """)
    Page<Todo> findByFilters(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId,
            @Param("completed") Boolean completed,
            @Param("priority") TodoPriority priority,
            @Param("noGoal") Boolean noGoal,
            Pageable pageable
    );

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    long countByGoalId(Long goalId);

    long countByGoalIdAndCompleted(Long goalId, Boolean completed);
}
