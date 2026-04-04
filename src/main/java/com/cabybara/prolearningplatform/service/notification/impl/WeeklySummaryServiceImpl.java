package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.IncorrectCardsByFlashcard;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.model.noti.NotificationPreference;
import com.cabybara.prolearningplatform.repository.NotificationPreferenceRepository;
import com.cabybara.prolearningplatform.repository.StudySessionReviewLogRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySummaryServiceImpl implements WeeklySummaryService {
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final StudySessionReviewLogRepository studySessionReviewLogRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final AuthenticationContext authenticationContext;

    @Override
    public void processWeeklySummaries() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        int todayValue = today.getValue();

        List<NotificationPreference> preferences = notificationPreferenceRepository.findEnabledByWeeklySummaryDay(todayValue);

        if (preferences.isEmpty()) {
            log.debug("No users scheduled for weekly summary on {}", today);
            return;
        }

        log.info("Processing weekly summary for {} users on {}", preferences.size(), today);

        int successCount = 0;
        for (NotificationPreference pref : preferences) {
            try {
                boolean sent = processSingleUser(pref);
                if (sent) successCount++;
            } catch (Exception e) {
                log.error("Failed to process weekly summary for user {}",
                        pref.getUser().getId(), e);
            }
        }


        log.info("Weekly summary completed: {}/{} users notified", successCount, preferences.size());
    }

    @Override
    @Transactional
    public void sendWeeklySummaryToUser(Long userId) {
        NotificationPreference pref = notificationPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No notification preference found for user " + userId));
        processSingleUser(pref);
    }

    @Transactional
    protected boolean processSingleUser(NotificationPreference pref) {
        Long userId = pref.getUser().getId();

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        OffsetDateTime periodFrom = calculatePeriodStart(pref).atZoneSameInstant(zone).toOffsetDateTime();
        OffsetDateTime periodTo   = OffsetDateTime.now(zone).toZonedDateTime().toOffsetDateTime();

        long incorrectCount = studySessionReviewLogRepository.countDistinctIncorrectCards(userId, periodFrom, periodTo);

        if (incorrectCount == 0) {
            log.debug("User {} has no incorrect cards in period [{}, {}]",
                    userId, periodFrom, periodTo);
            updateLastSummarySentAt(pref, periodTo);
            return false;
        }

        List<IncorrectCardsByFlashcard> breakdown = studySessionReviewLogRepository.findIncorrectCountGroupByFlashcard(userId, periodFrom, periodTo);

        List<Long> incorrectCardIds = studySessionReviewLogRepository
                .findDistinctIncorrectCardIds(userId, periodFrom, periodTo);

        CreateNotificationDto notification = buildSummaryNotification(
                userId, incorrectCount, incorrectCardIds, breakdown,
                periodFrom, periodTo, false);

        notificationDispatcher.dispatch(notification);
        log.info("Sent weekly summary to user {}: {} incorrect cards", userId, incorrectCount);

        updateLastSummarySentAt(pref, periodTo);

        return true;
    }

    private OffsetDateTime calculatePeriodStart(NotificationPreference pref) {
        if (pref.getLastSummarySentAt() != null) {
            return pref.getLastSummarySentAt();
        }

        return pref.getCreatedAt();
    }

    private void updateLastSummarySentAt(NotificationPreference pref, OffsetDateTime periodTo) {
        pref.setLastSummarySentAt(periodTo);
        notificationPreferenceRepository.save(pref);
    }

    private CreateNotificationDto buildSummaryNotification(
            Long userId,
            long incorrectCount,
            List<Long> incorrectCardIds,
            List<IncorrectCardsByFlashcard> breakdown,
            OffsetDateTime periodFrom,
            OffsetDateTime periodTo,
            boolean isPush) {

        if (!isPush) {
            isPush = true;
        }

        String message = String.format(
                "Trong khoang thoi gian qua, ban da tra loi sai %d the. " +
                        "Hay on lai de cung co kien thuc!",
                incorrectCount);

        Map<String, Object> flashcardBreakdown = breakdown.stream()
                .collect(Collectors.toMap(
                        b -> String.valueOf(b.getFlashcardId()),
                        IncorrectCardsByFlashcard::getIncorrectCount,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Object> data = new HashMap<>();
        data.put("incorrectCardIds", incorrectCardIds);
        data.put("incorrectCount", incorrectCount);
        data.put("flashcardBreakdown", flashcardBreakdown);
        data.put("periodFrom", periodFrom.toString());
        data.put("periodTo", periodTo.toString());
        data.put("canGenerateReviewSet", false);

        return CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.WEEKLY_SUMMARY)
                .title(NotificationType.WEEKLY_SUMMARY.getDefaultTitle())
                .message(message)
                .data(data)
                .actionUrl("/review/weekly-summary")
                .sendPush(isPush)
                .build();
    }
}
