package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSound;

public interface PomodoroSoundRepository extends JpaRepository<PomodoroSound, Long> {

    List<PomodoroSound> findBySourceAndIsActiveTrue(AssetSource source);

    List<PomodoroSound> findByUserIdAndIsActiveTrue(Long userId);

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    List<PomodoroSound> findAllAvailableForUser(@Param("userId") Long userId);

    Optional<PomodoroSound> findByIdAndUserId(Long id, Long userId);
}