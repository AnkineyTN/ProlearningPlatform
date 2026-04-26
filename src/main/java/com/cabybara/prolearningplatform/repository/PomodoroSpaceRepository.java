package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSpace;


public interface PomodoroSpaceRepository extends JpaRepository<PomodoroSpace, Long> {

    // Lấy tất cả SYSTEM space đang active
    List<PomodoroSpace> findBySourceAndIsActiveTrue(AssetSource source);

    // Lấy space của user
    List<PomodoroSpace> findByUserIdAndIsActiveTrue(Long userId);

    // Space kèm thông tin user đã favorite chưa
    @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    List<PomodoroSpace> findAllAvailableForUser(@Param("userId") Long userId);

    Optional<PomodoroSpace> findByIdAndUserId(Long id, Long userId);
}