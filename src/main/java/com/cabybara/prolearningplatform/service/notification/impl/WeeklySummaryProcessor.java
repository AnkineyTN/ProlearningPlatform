package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.helper.IncorrectCardsByFlashcard;
import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.model.noti.Notification;
import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;
import com.cabybara.prolearningplatform.repository.SetNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.repository.StudySessionReviewLogRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.review.ReviewBundleService;
import com.cabybara.prolearningplatform.utils.NotificationMessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySummaryProcessor {

    private static final ZoneId SUMMARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final SetNotificationPreferenceRepository setNotificationPreferenceRepository;
    private final StudySessionReviewLogRepository studySessionReviewLogRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final ReviewBundleService reviewBundleService;
    private final NotificationMessageResolver messageResolver;

    @Transactional
    public boolean processSingleSet(SetNotificationPreference pref) {
        Long setId = pref.getSet().getId();
        Long userId = pref.getSet().getUser().getId();

        OffsetDateTime periodFrom = calculatePeriodStart(pref).atZoneSameInstant(SUMMARY_ZONE).toOffsetDateTime();
        OffsetDateTime periodTo = OffsetDateTime.now(SUMMARY_ZONE).toZonedDateTime().toOffsetDateTime();

        long incorrectCount = studySessionReviewLogRepository
                .countDistinctIncorrectCardsBySet(userId, setId, periodFrom, periodTo);

        if (incorrectCount == 0) {
            log.debug("Set {} has no incorrect cards in period [{}, {}]", setId, periodFrom, periodTo);
            updateLastSummarySentAt(pref, periodTo);
            return false;
        }

        List<IncorrectCardsByFlashcard> breakdown = studySessionReviewLogRepository
                .findIncorrectCountGroupByFlashcardAndSet(userId, setId, periodFrom, periodTo);

        List<Long> incorrectCardIds = studySessionReviewLogRepository
                .findDistinctIncorrectCardIdsBySet(userId, setId, periodFrom, periodTo);

        ReviewBundle bundle = reviewBundleService.createBundle(
                userId, setId, incorrectCardIds, periodFrom, periodTo);

        UserLanguage language = pref.getSet().getUser().getLanguage() != null
                ? pref.getSet().getUser().getLanguage()
                : UserLanguage.EN;

        Notification notification = notificationDispatcher.dispatch(
                buildSummaryNotification(userId, setId, incorrectCount, breakdown, periodFrom, periodTo, bundle.getId(), language));

        bundle.setNotificationId(notification.getId());

        log.info("Sent weekly summary to user {} for set {}: {} incorrect cards, bundleId={}",
                userId, setId, incorrectCount, bundle.getId());

        updateLastSummarySentAt(pref, periodTo);
        return true;
    }

    private OffsetDateTime calculatePeriodStart(SetNotificationPreference pref) {
        if (pref.getLastSummarySentAt() != null) {
            return pref.getLastSummarySentAt();
        }
        return pref.getCreatedAt();
    }

    private void updateLastSummarySentAt(SetNotificationPreference pref, OffsetDateTime periodTo) {
        pref.setLastSummarySentAt(periodTo);
        setNotificationPreferenceRepository.save(pref);
    }

    private CreateNotificationDto buildSummaryNotification(
            Long userId,
            Long setId,
            long incorrectCount,
            List<IncorrectCardsByFlashcard> breakdown,
            OffsetDateTime periodFrom,
            OffsetDateTime periodTo,
            Long bundleId,
            UserLanguage language) {

        Map<String, Object> flashcardBreakdown = breakdown.stream()
                .collect(Collectors.toMap(
                        b -> String.valueOf(b.getFlashcardId()),
                        IncorrectCardsByFlashcard::getIncorrectCount,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Object> data = new HashMap<>();
        data.put("bundleId", bundleId);
        data.put("setId", setId);
        data.put("incorrectCount", incorrectCount);
        data.put("flashcardBreakdown", flashcardBreakdown);
        data.put("periodFrom", periodFrom.toString());
        data.put("periodTo", periodTo.toString());

        return CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.WEEKLY_SUMMARY)
                .title(messageResolver.resolve("notification.weekly_summary.title", language))
                .message(messageResolver.resolve("notification.weekly_summary.message", language, incorrectCount))
                .data(data)
                .actionUrl("/sets/" + setId + "/review-bundles/" + bundleId)
                .sendPush(true)
                .build();
    }
}
