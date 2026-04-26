package com.cabybara.prolearningplatform.dto.response.pomodoro;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class WeeklyStatsResponseDto {
    private Integer totalFocusMinutes;
    private Integer totalSessions;
    private Double completionRate;
    private List<DailyStatDto> dailyBreakdown;
    private DailyStatDto bestDay;
    private Integer currentStreak;

    @Data 
    @Builder
    public static class DailyStatDto {
        private LocalDate date;
        private Integer focusMinutes;
        private Integer sessions;
        private Integer completedSessions;
    }
}