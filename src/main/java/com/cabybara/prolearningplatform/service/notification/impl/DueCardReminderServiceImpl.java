package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.SetNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DueCardReminderServiceImpl implements DueCardReminderService {

    private final NotificationDispatcher notificationDispatcher;
    private final CardItemRepository cardItemRepository;
    private final SetNotificationPreferenceRepository setNotificationPreferenceRepository;

    @Override
    public void sendDueCardReminders() {
        Set<Long> enabledUserIds = Set.copyOf(
                setNotificationPreferenceRepository.findDistinctUserIdsByDueCardReminderEnabled());

        if (enabledUserIds.isEmpty()) {
            return;
        }

        List<UserDueStatDto> usersWithDueCards = cardItemRepository.findUsersWithDueCards(OffsetDateTime.now())
                .stream()
                .filter(stat -> enabledUserIds.contains(stat.getUserId()))
                .toList();

        if (usersWithDueCards.isEmpty()) {
            return;
        }

        List<CreateNotificationDto> notifications = usersWithDueCards.stream()
                .map(stat -> buildDueCardNotification(stat, "morning"))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
    }

    @Override
    public void sendEveningStudyReminders() {
        Set<Long> enabledUserIds = Set.copyOf(
                setNotificationPreferenceRepository.findDistinctUserIdsByDueCardReminderEnabled());

        if (enabledUserIds.isEmpty()) {
            log.info("No users with due card reminder enabled for evening reminder");
            return;
        }

        List<CreateNotificationDto> notifications = cardItemRepository.findUsersWithDueCards(OffsetDateTime.now())
                .stream()
                .filter(stat -> enabledUserIds.contains(stat.getUserId()) && stat.getDueCount() >= 5)
                .map(stat -> buildDueCardNotification(stat, "evening"))
                .toList();

        if (notifications.isEmpty()) {
            log.info("No users qualify for evening reminder");
            return;
        }

        notificationDispatcher.dispatchToMany(notifications);
    }

    @Override
    public void sendDueCardReminderToUser(Long userId, long dueCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", dueCount);

        CreateNotificationDto notification = CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.CARD_DUE_REMINDER)
                .title("Cards Due for Review")
                .message(String.format("You have %d cards waiting for review!", dueCount))
                .data(data)
                .actionUrl("/study")
                .sendPush(true)
                .build();

        notificationDispatcher.dispatch(notification);
    }

    private CreateNotificationDto buildDueCardNotification(UserDueStatDto stat, String timeOfDay) {
        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", stat.getDueCount());
        data.put("reminderType", timeOfDay);

        String message;
        String title;

        if ("morning".equals(timeOfDay)) {
            title = "Good Morning!";
            message = String.format("You have %d cards waiting for review. Start your day with a quick study session!",
                    stat.getDueCount());
        } else {
            title = "Evening Study Reminder";
            message = String.format("Don't forget! You still have %d cards to review today. Keep your streak going!",
                    stat.getDueCount());
        }

        return CreateNotificationDto.builder()
                .userId(stat.getUserId())
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(title)
                .message(message)
                .data(data)
                .actionUrl("/study")
                .sendPush(true)
                .build();
    }
}

