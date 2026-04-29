package com.cabybara.prolearningplatform.service.pomodoro.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.request.pomodoro.LogSessionRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.WeeklyStatsResponseDto;
import com.cabybara.prolearningplatform.enums.PomodoroSessionType;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSession;
import com.cabybara.prolearningplatform.repository.PomodoroSessionRepository;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSessionService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSessionServiceImpl implements PomodoroSessionService {

    private final PomodoroSessionRepository sessionRepository;
    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    @Override
    @Transactional
    public void logSession(LogSessionRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        PomodoroSession session = PomodoroSession.builder()
                .user(user)
                .type(dto.getType())
                .duration(dto.getDuration())
                .planned(dto.getPlanned())
                .completed(dto.getCompleted())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .build();

        sessionRepository.save(session);
    }

    @Override
    public WeeklyStatsResponseDto getWeeklyStats(LocalDate weekStart, String timezone) {
        Long userId = authenticationContext.getCurrentUserId();
        ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "UTC");

        OffsetDateTime from = weekStart.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime to   = weekStart.plusDays(7).atStartOfDay(zoneId).toOffsetDateTime();

        List<PomodoroSession> sessions = sessionRepository
                .findByUserIdAndDateRange(userId, from, to);

        // Group by date
        Map<LocalDate, List<PomodoroSession>> byDay = sessions.stream()
                .filter(s -> s.getType() == PomodoroSessionType.POMODORO)
                .collect(Collectors.groupingBy(s ->
                        s.getStartedAt().atZoneSameInstant(zoneId).toLocalDate()));

        List<WeeklyStatsResponseDto.DailyStatDto> dailyBreakdown = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<PomodoroSession> daySessions = byDay.getOrDefault(day, Collections.emptyList());
            int focusMinutes = daySessions.stream()
                    .mapToInt(PomodoroSession::getDuration).sum() / 60;
            long completed = daySessions.stream().filter(PomodoroSession::getCompleted).count();

            dailyBreakdown.add(WeeklyStatsResponseDto.DailyStatDto.builder()
                    .date(day)
                    .focusMinutes(focusMinutes)
                    .sessions(daySessions.size())
                    .completedSessions((int) completed)
                    .build());
        }

        int totalFocusMinutes = dailyBreakdown.stream()
                .mapToInt(WeeklyStatsResponseDto.DailyStatDto::getFocusMinutes).sum();
        int totalSessions = dailyBreakdown.stream()
                .mapToInt(WeeklyStatsResponseDto.DailyStatDto::getSessions).sum();
        int totalCompleted = dailyBreakdown.stream()
                .mapToInt(WeeklyStatsResponseDto.DailyStatDto::getCompletedSessions).sum();

        WeeklyStatsResponseDto.DailyStatDto bestDay = dailyBreakdown.stream()
                .max(Comparator.comparingInt(WeeklyStatsResponseDto.DailyStatDto::getFocusMinutes))
                .orElse(null);

        int streak = calculateStreak(userId, timezone);

        return WeeklyStatsResponseDto.builder()
                .totalFocusMinutes(totalFocusMinutes)
                .totalSessions(totalSessions)
                .completionRate(totalSessions > 0 ? (double) totalCompleted / totalSessions : 0.0)
                .dailyBreakdown(dailyBreakdown)
                .bestDay(bestDay)
                .currentStreak(streak)
                .build();
    }

    // Tính streak: đếm ngày liên tiếp có completed pomodoro, tính từ hôm qua về trước
    // (tránh streak reset ngay đầu ngày nếu chưa làm pomodoro nào)
    private int calculateStreak(Long userId, String timezone) {
        List<java.sql.Date> recentDays = sessionRepository
                .findRecentCompletedDays(userId, timezone != null ? timezone : "UTC");

        if (recentDays.isEmpty()) return 0;

        Set<LocalDate> daySet = recentDays.stream()
                .map(d -> d.toLocalDate())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now(ZoneId.of(timezone != null ? timezone : "UTC"));
        LocalDate check = daySet.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (daySet.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }
}