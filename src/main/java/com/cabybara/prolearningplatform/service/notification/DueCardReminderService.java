package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DueCardReminderService {

    private final NotificationDispatcher notificationDispatcher;
    private final CardItemRepository cardItemRepository;

    public void sendDueCardReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<UserDueStatDto> usersWithDueCards = cardItemRepository.findUsersWithDueCards(now);

        if (usersWithDueCards.isEmpty()) {
            return;
        }

        List<CreateNotificationDto> notifications = usersWithDueCards.stream()
                .map(stat -> buildDueCardNotification(stat, "morning"))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
    }

    public void sendEveningStudyReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<UserDueStatDto> usersWithDueCards = cardItemRepository.findUsersWithDueCards(now);

        if (usersWithDueCards.isEmpty()) {
            log.info("No users with due cards for evening reminder");
            return;
        }

        List<CreateNotificationDto> notifications = usersWithDueCards.stream()
                .filter(stat -> stat.getDueCount() >= 5)
                .map(stat -> buildDueCardNotification(stat, "evening"))
                .toList();

        if (notifications.isEmpty()) {
            log.info("No users qualify for evening reminder");
            return;
        }

        notificationDispatcher.dispatchToMany(notifications);
    }

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

