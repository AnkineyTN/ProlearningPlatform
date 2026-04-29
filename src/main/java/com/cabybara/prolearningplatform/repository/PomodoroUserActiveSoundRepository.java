package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserActiveSound;

public interface PomodoroUserActiveSoundRepository extends JpaRepository<PomodoroUserActiveSound, Long> {
    List<PomodoroUserActiveSound> findByUserId(Long userId);

    Optional<PomodoroUserActiveSound> findByUserIdAndSoundId(Long userId, Long soundId);

    boolean existsByUserIdAndSoundId(Long userId, Long soundId);

    @Modifying
    @Query("DELETE FROM PomodoroUserActiveSound u WHERE u.user.id = :userId AND u.sound.id = :soundId")
    void deleteByUserIdAndSoundId(@Param("userId") Long userId, @Param("soundId") Long soundId);

    @Modifying
    @Query("DELETE FROM PomodoroUserActiveSound u WHERE u.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
