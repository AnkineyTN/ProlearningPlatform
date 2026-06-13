package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.UserDueFlashcardProjection;
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

        List<UserDueFlashcardProjection> dueFlashcards = cardItemRepository.findDueFlashcardsByUser(now)
                .stream()
                .filter(f -> enabledUserIds.contains(f.getUserId()))
                .filter(f -> !recentlyActiveUserIds.contains(f.getUserId()))
                .toList();

        if (dueFlashcards.isEmpty()) {
            return;
        }

        Map<Long, List<Map<String, Object>>> flashcardsByUser = new LinkedHashMap<>();
        Map<Long, Long> dueCountByUser = new LinkedHashMap<>();
        Map<Long, UserLanguage> languageByUser = new HashMap<>();
        for (UserDueFlashcardProjection flashcard : dueFlashcards) {
            flashcardsByUser.computeIfAbsent(flashcard.getUserId(), k -> new ArrayList<>())
                    .add(buildFlashcardPayload(flashcard));
            dueCountByUser.merge(flashcard.getUserId(), flashcard.getDueCount(), Long::sum);
            languageByUser.putIfAbsent(flashcard.getUserId(),
                    flashcard.getUserLanguage() != null ? flashcard.getUserLanguage() : UserLanguage.EN);
        }

        List<CreateNotificationDto> notifications = new ArrayList<>();
        for (Map.Entry<Long, List<Map<String, Object>>> entry : flashcardsByUser.entrySet()) {
            Long userId = entry.getKey();
            long dueCount = dueCountByUser.getOrDefault(userId, 0L);
            if (dueCount < MIN_DUE_CARDS_TO_NOTIFY) {
                continue;
            }
            notifications.add(buildDueCardNotification(userId, dueCount, entry.getValue(), languageByUser.get(userId)));
        }

        if (!notifications.isEmpty()) {
            notificationDispatcher.dispatchToMany(notifications);
        }
    }

    @Override
    public void sendDueCardReminderToUser(Long userId, long dueCount) {
        List<Map<String, Object>> dueFlashcards = cardItemRepository.findDueFlashcardsByUser(OffsetDateTime.now())
                .stream()
                .filter(f -> userId.equals(f.getUserId()))
                .map(this::buildFlashcardPayload)
                .toList();

        Map<String, Object> data = buildDueCardNotificationData(dueCount, dueFlashcards);

        CreateNotificationDto notification = CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(messageResolver.resolve("notification.due_card.direct.title", UserLanguage.EN))
                .message(messageResolver.resolve("notification.due_card.direct.message", UserLanguage.EN, dueCount))
                .data(data)
                .actionUrl("/review/due-cards")
                .sendPush(true)
                .build();

        notificationDispatcher.dispatch(notification);
    }

    private CreateNotificationDto buildDueCardNotification(Long userId,
                                                            long dueCount,
                                                            List<Map<String, Object>> dueFlashcards,
                                                            UserLanguage language) {
        Map<String, Object> data = buildDueCardNotificationData(dueCount, dueFlashcards);

        return CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.CARD_DUE_REMINDER)
                .title(messageResolver.resolve("notification.due_card.direct.title", language))
                .message(messageResolver.resolve("notification.due_card.direct.message", language, dueCount))
                .data(data)
                .actionUrl("/review/due-cards")
                .sendPush(true)
                .build();
    }

    private Map<String, Object> buildDueCardNotificationData(long dueCount,
                                                             List<Map<String, Object>> dueFlashcards) {
        Map<String, Object> data = new HashMap<>();
        data.put("dueCount", dueCount);
        data.put("flashcards", dueFlashcards);
        return data;
    }

    private Map<String, Object> buildFlashcardPayload(UserDueFlashcardProjection flashcard) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("setId", flashcard.getSetId());
        payload.put("flashcardId", flashcard.getFlashcardId());
        payload.put("flashcardTitle", flashcard.getFlashcardTitle());
        payload.put("dueCount", flashcard.getDueCount());
        return payload;
    }
}
