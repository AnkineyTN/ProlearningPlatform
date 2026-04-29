package com.cabybara.prolearningplatform.service.activity.impl;

import com.cabybara.prolearningplatform.dto.request.activity.ActivityLogRequestDto;
import com.cabybara.prolearningplatform.dto.response.activity.ActivitySummaryResponseDto;
import com.cabybara.prolearningplatform.dto.response.activity.ContentTypeSummaryDto;
import com.cabybara.prolearningplatform.dto.response.activity.HeatmapDayDto;
import com.cabybara.prolearningplatform.dto.response.activity.StreakResponseDto;
import com.cabybara.prolearningplatform.model.ActivityLog;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.ActivityLogRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.activity.ActivityLogService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final ZoneId ICT = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final long MIN_ACTIVE_SECONDS = 30;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public void logActivity(ActivityLogRequestDto dto) {
        if (dto.getActiveDuration() < MIN_ACTIVE_SECONDS) {
            return;
        }
        if (dto.getActiveDuration() > dto.getRawDuration()) {
            throw new IllegalArgumentException("activeDuration cannot exceed rawDuration");
        }

        Long userId = authenticationContext.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        LocalDate date = OffsetDateTime.parse(dto.getClientTimestamp())
                .atZoneSameInstant(ICT)
                .toLocalDate();

        Optional<ActivityLog> existing = activityLogRepository
                .findByUserIdAndDateAndContentTypeAndSetId(userId, date, dto.getContentType(), dto.getSetId());

        if (existing.isPresent()) {
            ActivityLog log = existing.get();
            log.setActiveDuration(log.getActiveDuration() + dto.getActiveDuration());
            log.setRawDuration(log.getRawDuration() + dto.getRawDuration());
            log.setItemsCount(log.getItemsCount() + (dto.getItemsCount() != null ? dto.getItemsCount() : 0));
            if (dto.getScore() != null) {
                log.setScore(log.getScore() == null ? dto.getScore() : Math.max(log.getScore(), dto.getScore()));
            }
            activityLogRepository.save(log);
        } else {
            // First log of this day: consume freeze token if resuming after a 2-day gap
            List<LocalDate> prevActiveDays = activityLogRepository.findActiveDays(userId);
            if (!prevActiveDays.isEmpty()) {
                long gap = date.toEpochDay() - prevActiveDays.get(0).toEpochDay();
                if (gap == 2 && user.getStreakFreezeTokens() > 0) {
                    user.setStreakFreezeTokens(user.getStreakFreezeTokens() - 1);
                    userRepository.save(user);
                }
            }
            ActivityLog log = ActivityLog.builder()
                    .user(user)
                    .date(date)
                    .contentType(dto.getContentType())
                    .setId(dto.getSetId())
                    .todoId(dto.getTodoId())
                    .activeDuration(dto.getActiveDuration())
                    .rawDuration(dto.getRawDuration())
                    .score(dto.getScore())
                    .itemsCount(dto.getItemsCount() != null ? dto.getItemsCount() : 0)
                    .build();
            activityLogRepository.save(log);
        }
    }

    @Override
    public List<HeatmapDayDto> getHeatmap(int months) {
        Long userId = authenticationContext.getCurrentUserId();
        LocalDate endDate = LocalDate.now(ICT);
        LocalDate startDate = endDate.minusMonths(Math.min(Math.max(months, 1), 12));

        List<Object[]> rows = activityLogRepository.findHeatmapData(userId, startDate, endDate);
        List<HeatmapDayDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            long totalSeconds = toLong(row[1]);
            long sessions = toLong(row[2]);
            Integer bestScore = row[3] != null ? toInt(row[3]) : null;
            long totalItems = toLong(row[4]);

            result.add(HeatmapDayDto.builder()
                    .date(date.format(DATE_FMT))
                    .totalMinutes(totalSeconds / 60)
                    .sessions(sessions)
                    .bestScore(bestScore)
                    .totalItems(totalItems)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional
    public StreakResponseDto getStreak() {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<LocalDate> activeDays = activityLogRepository.findActiveDays(userId);

        if (activeDays.isEmpty()) {
            return StreakResponseDto.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .lastActiveDate(null)
                    .studiedToday(false)
                    .build();
        }

        LocalDate today = LocalDate.now(ICT);
        LocalDate lastActive = activeDays.get(0);
        long daysDiff = today.toEpochDay() - lastActive.toEpochDay();

        if (daysDiff > 1) {
            if (daysDiff == 2 && user.getStreakFreezeTokens() > 0) {
                // Freeze token covers the gap; token will be consumed on the next logActivity
                // call
            } else {
                int longest = computeLongestStreak(activeDays);
                return StreakResponseDto.builder()
                        .currentStreak(0)
                        .longestStreak(longest)
                        .lastActiveDate(lastActive.format(DATE_FMT))
                        .studiedToday(false)
                        .build();
            }
        }

        int current = computeCurrentStreak(activeDays);
        int longest = computeLongestStreak(activeDays);
        boolean studiedToday = lastActive.equals(today);

        return StreakResponseDto.builder()
                .currentStreak(current)
                .longestStreak(longest)
                .lastActiveDate(lastActive.format(DATE_FMT))
                .studiedToday(studiedToday)
                .build();
    }

    @Override
    public ActivitySummaryResponseDto getSummary(int days) {
        Long userId = authenticationContext.getCurrentUserId();
        int clampedDays = Math.min(Math.max(days, 1), 365);
        LocalDate startDate = LocalDate.now(ICT).minusDays(clampedDays);

        List<Object[]> overallList = activityLogRepository.findOverallSummary(userId, startDate);
        Object[] overall = overallList.isEmpty() ? new Object[4] : overallList.get(0);
        long totalSeconds = overall[0] != null ? toLong(overall[0]) : 0L;
        long totalSessions = overall[1] != null ? toLong(overall[1]) : 0L;
        long totalItems = overall[2] != null ? toLong(overall[2]) : 0L;
        Double avgScore = overall[3] != null ? toDouble(overall[3]) : null;

        List<Object[]> breakdownRows = activityLogRepository.findSummaryByContentType(userId, startDate);
        List<ContentTypeSummaryDto> breakdown = new ArrayList<>();
        for (Object[] row : breakdownRows) {
            breakdown.add(ContentTypeSummaryDto.builder()
                    .contentType(String.valueOf(row[0]))
                    .totalMinutes(toLong(row[1]) / 60)
                    .sessions(toLong(row[2]))
                    .build());
        }

        return ActivitySummaryResponseDto.builder()
                .totalMinutes(totalSeconds / 60)
                .totalSessions(totalSessions)
                .totalItems(totalItems)
                .avgExamScore(avgScore)
                .breakdown(breakdown)
                .build();
    }

    // --- Streak helpers ---

    private int computeCurrentStreak(List<LocalDate> activeDays) {
        if (activeDays.isEmpty())
            return 0;
        int streak = 1;
        for (int i = 1; i < activeDays.size(); i++) {
            long gap = activeDays.get(i - 1).toEpochDay() - activeDays.get(i).toEpochDay();
            if (gap == 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private int computeLongestStreak(List<LocalDate> activeDays) {
        if (activeDays.isEmpty())
            return 0;
        int longest = 1;
        int current = 1;
        for (int i = 1; i < activeDays.size(); i++) {
            long gap = activeDays.get(i - 1).toEpochDay() - activeDays.get(i).toEpochDay();
            if (gap == 1) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }
        return longest;
    }

    // --- Type conversion helpers for native query Object[] ---

    private LocalDate toLocalDate(Object o) {
        if (o instanceof LocalDate d)
            return d;
        if (o instanceof java.sql.Date d)
            return d.toLocalDate();
        return LocalDate.parse(o.toString());
    }

    private long toLong(Object o) {
        if (o == null)
            return 0L;
        if (o instanceof BigInteger bi)
            return bi.longValue();
        if (o instanceof BigDecimal bd)
            return bd.longValue();
        if (o instanceof Number n)
            return n.longValue();
        return Long.parseLong(o.toString());
    }

    private int toInt(Object o) {
        if (o instanceof BigInteger bi)
            return bi.intValue();
        if (o instanceof Number n)
            return n.intValue();
        return Integer.parseInt(o.toString());
    }

    private double toDouble(Object o) {
        if (o instanceof BigDecimal bd)
            return bd.doubleValue();
        if (o instanceof Number n)
            return n.doubleValue();
        return Double.parseDouble(o.toString());
    }
}
