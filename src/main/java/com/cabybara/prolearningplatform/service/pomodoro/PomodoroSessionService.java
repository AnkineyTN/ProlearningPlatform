package com.cabybara.prolearningplatform.service.pomodoro;

import java.time.LocalDate;

import com.cabybara.prolearningplatform.dto.request.pomodoro.LogSessionRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.WeeklyStatsResponseDto;

public interface PomodoroSessionService {
    void logSession(LogSessionRequestDto dto);
    WeeklyStatsResponseDto getWeeklyStats(LocalDate weekStart, String timezone);
}
