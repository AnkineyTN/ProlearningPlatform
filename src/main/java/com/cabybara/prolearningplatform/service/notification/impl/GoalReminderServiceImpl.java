package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.repository.GoalRepository;
import com.cabybara.prolearningplatform.repository.UserNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.GoalReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.utils.NotificationMessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalReminderServiceImpl implements GoalReminderService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int[] DEADLINE_DAYS = {1, 7, 30};
    private static final int INACTIVE_THRESHOLD_DAYS = 7;

    private final GoalRepository goalRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final NotificationMessageResolver messageResolver;

    @Override
    @Transactional
    public void sendGoalDeadlineReminders() {
        LocalDate today = LocalDate.now(ZONE);
        int currentHour = LocalTime.now(ZONE).getHour();

        List<Long> userIds = preferenceRepository.findUserIdsForGoalDeadlineReminderDue(currentHour, today);
        if (userIds.isEmpty()) return;

        List<CreateNotificationDto> notifications = new ArrayList<>();
        for (int days : DEADLINE_DAYS) {
            List<Goal> goals = goalRepository.findGoalsWithTargetDate(userIds, today.plusDays(days));
            for (Goal goal : goals) {
                UserLanguage lang = resolveLanguage(goal);
                notifications.add(buildDeadlineNotification(goal, lang, days));
            }
        }

        if (!notifications.isEmpty()) {
            notificationDispatcher.dispatchToMany(notifications);
            log.debug("Sent goal deadline reminders: {} notifications", notifications.size());
        }

        preferenceRepository.markGoalDeadlineReminderSent(userIds, today);
    }

    @Override
    @Transactional
    public void sendGoalInactiveReminders() {
        LocalDate today = LocalDate.now(ZONE);
        if (today.getDayOfWeek() != DayOfWeek.MONDAY) return;

        int currentHour = LocalTime.now(ZONE).getHour();
        List<Long> userIds = preferenceRepository.findUserIdsForGoalInactiveReminderDue(currentHour, today);
        if (userIds.isEmpty()) return;

        OffsetDateTime cutoff = OffsetDateTime.now(ZONE).minusDays(INACTIVE_THRESHOLD_DAYS);
        List<Goal> inactiveGoals = goalRepository.findInactiveGoals(userIds, cutoff);

        if (!inactiveGoals.isEmpty()) {
            List<CreateNotificationDto> notifications = inactiveGoals.stream()
                    .map(g -> buildInactiveNotification(g, resolveLanguage(g)))
                    .toList();
            notificationDispatcher.dispatchToMany(notifications);
            log.debug("Sent goal inactive reminders to {} goals", notifications.size());
        }

        preferenceRepository.markGoalInactiveReminderSent(userIds, today);
    }

    @Override
    public void debugSendGoalDeadlineReminders() {
        List<Long> userIds = preferenceRepository.findAllUserIdsWithGoalDeadlineReminderEnabled();
        if (userIds.isEmpty()) return;

        LocalDate today = LocalDate.now(ZONE);
        List<CreateNotificationDto> notifications = new ArrayList<>();

        for (int days : DEADLINE_DAYS) {
            List<Goal> goals = goalRepository.findGoalsWithTargetDate(userIds, today.plusDays(days));
            for (Goal goal : goals) {
                UserLanguage lang = resolveLanguage(goal);
                notifications.add(buildDeadlineNotification(goal, lang, days));
            }
        }

        if (!notifications.isEmpty()) {
            notificationDispatcher.dispatchToMany(notifications);
            log.debug("Debug: sent goal deadline reminders: {} notifications", notifications.size());
        }
    }

    @Override
    public void debugSendGoalInactiveReminders() {
        List<Long> userIds = preferenceRepository.findAllUserIdsWithGoalInactiveReminderEnabled();
        if (userIds.isEmpty()) return;

        OffsetDateTime cutoff = OffsetDateTime.now(ZONE).minusDays(INACTIVE_THRESHOLD_DAYS);
        List<Goal> inactiveGoals = goalRepository.findInactiveGoals(userIds, cutoff);
        if (inactiveGoals.isEmpty()) return;

        List<CreateNotificationDto> notifications = inactiveGoals.stream()
                .map(g -> buildInactiveNotification(g, resolveLanguage(g)))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
        log.debug("Debug: sent goal inactive reminders to {} goals", notifications.size());
    }

    private CreateNotificationDto buildDeadlineNotification(Goal goal, UserLanguage lang, int days) {
        String timeKey = switch (days) {
            case 1 -> "notification.goal_deadline.time.1day";
            case 7 -> "notification.goal_deadline.time.1week";
            default -> "notification.goal_deadline.time.1month";
        };
        String timeLabel = messageResolver.resolve(timeKey, lang);

        return CreateNotificationDto.builder()
                .userId(goal.getUser().getId())
                .type(NotificationType.GOAL_DEADLINE_REMINDER)
                .title(messageResolver.resolve("notification.goal_deadline.title", lang))
                .message(messageResolver.resolve("notification.goal_deadline.message", lang, goal.getTitle(), timeLabel))
                .actionUrl("/goals/" + goal.getId())
                .sendPush(true)
                .data(Map.of("goalTitle", goal.getTitle(), "days", days))
                .build();
    }

    private CreateNotificationDto buildInactiveNotification(Goal goal, UserLanguage lang) {
        return CreateNotificationDto.builder()
                .userId(goal.getUser().getId())
                .type(NotificationType.GOAL_INACTIVE_REMINDER)
                .title(messageResolver.resolve("notification.goal_inactive.title", lang))
                .message(messageResolver.resolve("notification.goal_inactive.message", lang, goal.getTitle()))
                .actionUrl("/goals/" + goal.getId())
                .sendPush(true)
                .data(Map.of("goalTitle", goal.getTitle()))
                .build();
    }

    private UserLanguage resolveLanguage(Goal goal) {
        UserLanguage lang = goal.getUser().getLanguage();
        return lang != null ? lang : UserLanguage.EN;
    }
}
