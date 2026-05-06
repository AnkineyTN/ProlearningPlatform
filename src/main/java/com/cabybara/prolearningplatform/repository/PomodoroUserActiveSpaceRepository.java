package com.cabybara.prolearningplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserActiveSpace;

public interface PomodoroUserActiveSpaceRepository extends JpaRepository<PomodoroUserActiveSpace, Long> {

    Optional<PomodoroUserActiveSpace> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM PomodoroUserActiveSpace u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}