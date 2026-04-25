package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.UserNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.utils.NotificationMessageResolver;
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
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final NotificationMessageResolver messageResolver;

    @Override
    public void sendDueCardReminders() {
        Set<Long> enabledUserIds = Set.copyOf(
                userNotificationPreferenceRepository.findUserIdsByDueCardReminderEnabled());

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
                .map(stat -> buildDueCardNotification(stat))
                .toList();

        notificationDispatcher.dispatchToMany(notifications);
    }

//    @Override
//    public void sendEveningStudyReminders() {
//        Set<Long> enabledUserIds = Set.copyOf(
//                userNotificationPreferenceRepository.findUserIdsByDueCardReminderEnabled());
//
//        if (enabledUserIds.isEmpty()) {
//            log.info("No users with due card reminder enabled for evening reminder");
//            return;
//        }
//
//        List<CreateNotificationDto> notifications = cardItemRepository.findUsersWithDueCards(OffsetDateTime.now())
//                .stream()
//                .filter(stat -> enabledUserIds.contains(stat.getUserId()) && stat.getDueCount() >= 5)
//                .map(stat -> buildDueCardNotification(stat))
//                .toList();
//
//        if (notifications.isEmpty()) {
//            log.info("No users qualify for evening reminder");
//            return;
//        }
//
//        notificationDispatcher.dispatchToMany(notifications);
//    }

    @Override
    public void sendDueCardReminderToUser(Long userId, long dueCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", dueCount);

        CreateNotificationDto notification = CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(messageResolver.resolve("notification.due_card.direct.title", UserLanguage.EN))
                .message(messageResolver.resolve("notification.due_card.direct.message", UserLanguage.EN, dueCount))
                .data(data)
                .actionUrl("/flascards")
                .sendPush(true)
                .build();

        notificationDispatcher.dispatch(notification);
    }

    private CreateNotificationDto buildDueCardNotification(UserDueStatDto stat) {
        UserLanguage language = stat.getUserLanguage() != null ? stat.getUserLanguage() : UserLanguage.EN;

        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", stat.getDueCount());

        String titleKey = "notification.due_card.direct.title";
        String messageKey = "notification.due_card.direct.message";

        return CreateNotificationDto.builder()
                .userId(stat.getUserId())
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(messageResolver.resolve(titleKey, language))
                .message(messageResolver.resolve(messageKey, language, stat.getDueCount()))
                .data(data)
                .actionUrl("/flashcards")
                .sendPush(true)
                .build();
    }
}

