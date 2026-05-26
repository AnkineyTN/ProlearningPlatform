package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.UserCalendarSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCalendarSettingRepository extends JpaRepository<UserCalendarSetting, Long> {
    Optional<UserCalendarSetting> findByUserId(Long userId);
}
