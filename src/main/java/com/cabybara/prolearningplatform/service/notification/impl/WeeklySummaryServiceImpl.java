package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;
import com.cabybara.prolearningplatform.repository.SetNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySummaryServiceImpl implements WeeklySummaryService {

    private static final ZoneId SUMMARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final SetNotificationPreferenceRepository setNotificationPreferenceRepository;
    private final WeeklySummaryProcessor processor;

    @Override
    public void processWeeklySummaries() {
        DayOfWeek today = LocalDate.now(SUMMARY_ZONE).getDayOfWeek();
        int todayValue = today.getValue();

        List<SetNotificationPreference> preferences =
                setNotificationPreferenceRepository.findAllByWeeklySummaryDay(todayValue);

        if (preferences.isEmpty()) {
            log.debug("No sets scheduled for weekly summary on {}", today);
            return;
        }

        log.info("Processing weekly summary for {} sets on {}", preferences.size(), today);

        int successCount = 0;
        for (SetNotificationPreference pref : preferences) {
            try {
                boolean sent = processor.processSingleSet(pref);
                if (sent) successCount++;
            } catch (Exception e) {
                log.error("Failed to process weekly summary for set {}", pref.getSet().getId(), e);
            }
        }

        log.info("Weekly summary completed: {}/{} sets notified", successCount, preferences.size());
    }

    @Override
    public void sendWeeklySummaryToUser(Long userId) {
        List<SetNotificationPreference> preferences =
                setNotificationPreferenceRepository.findEnabledByUserId(userId);

        if (preferences.isEmpty()) {
            log.debug("No sets with weekly summary enabled for user {}", userId);
            return;
        }

        for (SetNotificationPreference pref : preferences) {
            processor.processSingleSet(pref);
        }
    }
}
