package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.UserTodoCountProjection;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.repository.TodoRepository;
import com.cabybara.prolearningplatform.repository.UserNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.notification.TodoReminderService;
import com.cabybara.prolearningplatform.utils.NotificationMessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoReminderServiceImpl implements TodoReminderService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final TodoRepository todoRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final NotificationMessageResolver messageResolver;

    @Override
    public void sendDailyTodoReminders() {
        int currentHour = LocalTime.now(ZONE).getHour();
        List<Long> userIds = preferenceRepository.findUserIdsForDailyTodoReminder(currentHour);
        if (userIds.isEmpty()) return;

        LocalDate today = LocalDate.now(ZONE);
        List<UserTodoCountProjection> counts = todoRepository.countIncompleteDailyTodosForUsers(userIds, today);
        if (counts.isEmpty()) return;

        List<CreateNotificationDto> notifications = counts.stream()
                .map(c -> buildNotification(
                        c.getUserId(), c.getCount(),
                        c.getUserLanguage() != null ? c.getUserLanguage() : UserLanguage.EN,
                        NotificationType.DAILY_TODO_REMINDER))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
        log.debug("Sent daily todo reminders to {} users", notifications.size());
    }

    @Override
    public void sendWeeklyTodoReminders() {
        LocalDate today = LocalDate.now(ZONE);
        if (today.getDayOfWeek() != DayOfWeek.SUNDAY) return;

        int currentHour = LocalTime.now(ZONE).getHour();
        List<Long> userIds = preferenceRepository.findUserIdsForWeeklyTodoReminder(currentHour);
        if (userIds.isEmpty()) return;

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<UserTodoCountProjection> counts = todoRepository.countIncompleteWeeklyTodosForUsers(userIds, weekStart, today);
        if (counts.isEmpty()) return;

        List<CreateNotificationDto> notifications = counts.stream()
                .map(c -> buildNotification(
                        c.getUserId(), c.getCount(),
                        c.getUserLanguage() != null ? c.getUserLanguage() : UserLanguage.EN,
                        NotificationType.WEEKLY_TODO_REMINDER))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
        log.debug("Sent weekly todo reminders to {} users", notifications.size());
    }

    @Override
    public void debugSendDailyTodoReminders() {
        List<Long> userIds = preferenceRepository.findAllUserIdsWithDailyTodoReminderEnabled();
        if (userIds.isEmpty()) return;

        LocalDate today = LocalDate.now(ZONE);
        List<UserTodoCountProjection> counts = todoRepository.countIncompleteDailyTodosForUsers(userIds, today);
        if (counts.isEmpty()) return;

        List<CreateNotificationDto> notifications = counts.stream()
                .map(c -> buildNotification(
                        c.getUserId(), c.getCount(),
                        c.getUserLanguage() != null ? c.getUserLanguage() : UserLanguage.EN,
                        NotificationType.DAILY_TODO_REMINDER))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
        log.debug("Debug: sent daily todo reminders to {} users", notifications.size());
    }

    @Override
    public void debugSendWeeklyTodoReminders() {
        List<Long> userIds = preferenceRepository.findAllUserIdsWithWeeklyTodoReminderEnabled();
        if (userIds.isEmpty()) return;

        LocalDate today = LocalDate.now(ZONE);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<UserTodoCountProjection> counts = todoRepository.countIncompleteWeeklyTodosForUsers(userIds, weekStart, today);
        if (counts.isEmpty()) return;

        List<CreateNotificationDto> notifications = counts.stream()
                .map(c -> buildNotification(
                        c.getUserId(), c.getCount(),
                        c.getUserLanguage() != null ? c.getUserLanguage() : UserLanguage.EN,
                        NotificationType.WEEKLY_TODO_REMINDER))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
        log.debug("Debug: sent weekly todo reminders to {} users", notifications.size());
    }

    private CreateNotificationDto buildNotification(Long userId, long count, UserLanguage language, NotificationType type) {
        boolean isDaily = type == NotificationType.DAILY_TODO_REMINDER;
        String titleKey = isDaily ? "notification.daily_todo.title" : "notification.weekly_todo.title";
        String messageKey = isDaily ? "notification.daily_todo.message" : "notification.weekly_todo.message";

        return CreateNotificationDto.builder()
                .userId(userId)
                .type(type)
                .title(messageResolver.resolve(titleKey, language))
                .message(messageResolver.resolve(messageKey, language, count))
                .actionUrl("/todos")
                .sendPush(true)
                .data(Map.of("count", count))
                .build();
    }
}
