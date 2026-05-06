package com.cabybara.prolearningplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSetting;

public interface PomodoroSettingRepository extends JpaRepository<PomodoroSetting, Long> {
    Optional<PomodoroSetting> findByUserId(Long userId);
}
