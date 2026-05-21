package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.AppealStatus;
import com.cabybara.prolearningplatform.model.UserAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAppealRepository extends JpaRepository<UserAppeal, Long> {
    Page<UserAppeal> findByStatus(AppealStatus status, Pageable pageable);
    List<UserAppeal> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndStatus(Long userId, AppealStatus status);
}
