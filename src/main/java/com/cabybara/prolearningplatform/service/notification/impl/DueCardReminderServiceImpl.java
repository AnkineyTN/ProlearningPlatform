package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.UserDueCardProjection;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.StudySessionReviewLogRepository;
import com.cabybara.prolearningplatform.repository.UserNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.utils.NotificationMessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DueCardReminderServiceImpl implements DueCardReminderService {

    private static final int MIN_DUE_CARDS_TO_NOTIFY = 5;
    private static final int RECENTLY_ACTIVE_HOURS = 24;

    private final NotificationDispatcher notificationDispatcher;
    private final CardItemRepository cardItemRepository;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final StudySessionReviewLogRepository studySessionReviewLogRepository;
    private final NotificationMessageResolver messageResolver;

    @Override
    public void sendDueCardReminders() {
        Set<Long> enabledUserIds = Set.copyOf(
                userNotificationPreferenceRepository.findUserIdsByDueCardReminderEnabled());

        if (enabledUserIds.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        Set<Long> recentlyActiveUserIds = Set.copyOf(
                studySessionReviewLogRepository.findUserIdsWithActivitySince(now.minusHours(RECENTLY_ACTIVE_HOURS)));

        List<UserDueCardProjection> dueCards = cardItemRepository.findDueCardsByUser(now)
                .stream()
                .filter(c -> enabledUserIds.contains(c.getUserId()))
                .filter(c -> !recentlyActiveUserIds.contains(c.getUserId()))
                .toList();

        if (dueCards.isEmpty()) {
            return;
        }

        Map<Long, List<Long>> cardIdsByUser = new LinkedHashMap<>();
        Map<Long, UserLanguage> languageByUser = new HashMap<>();
        for (UserDueCardProjection card : dueCards) {
            cardIdsByUser.computeIfAbsent(card.getUserId(), k -> new ArrayList<>()).add(card.getCardId());
            languageByUser.putIfAbsent(card.getUserId(),
                    card.getUserLanguage() != null ? card.getUserLanguage() : UserLanguage.EN);
        }

        List<CreateNotificationDto> notifications = new ArrayList<>();
        for (Map.Entry<Long, List<Long>> entry : cardIdsByUser.entrySet()) {
            Long userId = entry.getKey();
            int dueCount = entry.getValue().size();
            if (dueCount < MIN_DUE_CARDS_TO_NOTIFY) {
                continue;
            }
            notifications.add(buildDueCardNotification(userId, dueCount, languageByUser.get(userId)));
        }

        if (!notifications.isEmpty()) {
            notificationDispatcher.dispatchToMany(notifications);
        }
    }

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
                .actionUrl("/flashcards")
                .sendPush(true)
                .build();

        notificationDispatcher.dispatch(notification);
    }

    private CreateNotificationDto buildDueCardNotification(Long userId, long dueCount, UserLanguage language) {
        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", dueCount);

        return CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(messageResolver.resolve("notification.due_card.direct.title", language))
                .message(messageResolver.resolve("notification.due_card.direct.message", language, dueCount))
                .data(data)
                .actionUrl("/flashcards")
                .sendPush(true)
                .build();
    }
}
