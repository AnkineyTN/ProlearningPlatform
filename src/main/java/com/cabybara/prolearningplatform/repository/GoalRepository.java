package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    Page<Goal> findByUserId(Long userId, Pageable pageable);

    Page<Goal> findByUserIdAndStatus(Long userId, GoalStatus status, Pageable pageable);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);
}
