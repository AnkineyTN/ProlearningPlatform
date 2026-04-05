package com.cabybara.prolearningplatform.service.scheduler.impl;

import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import com.cabybara.prolearningplatform.service.scheduler.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler implements NotificationSchedulerService {
    private static final String SCHEDULER_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private final DueCardReminderService dueCardReminderService;
    private final NotificationService notificationService;
    private final WeeklySummaryService weeklySummaryService;

    @Value("${notification.cleanup.days-old:30}")
    private int cleanupDaysOld;

    @Scheduled(cron = "${notification.due-card-reminder.cron:0 0 8 * * *}", zone = SCHEDULER_TIME_ZONE)
    public void sendDueCardReminders() {
        try {
            dueCardReminderService.sendDueCardReminders();
        } catch (Exception e) {
            log.error("Error in scheduled job (due card reminders): {}", e.getMessage(), e);
        }
    }

//    @Scheduled(cron = "${notification.evening-reminder.cron:0 0 19 * * *}", zone = SCHEDULER_TIME_ZONE)
//    public void sendEveningStudyReminders() {
//        try {
//            dueCardReminderService.sendEveningStudyReminders();
//        } catch (Exception e) {
//            log.error("Error in scheduled job (due card reminders): {}", e.getMessage(), e);
//        }
//    }

    @Scheduled(cron = "${notification.weekly-summary.cron:0 0 9 * * *}", zone = SCHEDULER_TIME_ZONE)
    public void processWeeklySummaries() {
        try {
            weeklySummaryService.processWeeklySummaries();
        } catch (Exception e) {
            log.error("Error in scheduled job (weekly summaries): {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${notification.cleanup.cron:0 0 2 * * SUN}", zone = SCHEDULER_TIME_ZONE)
    public void cleanupOldNotifications() {
        try {
            int deleted = notificationService.cleanupOldNotifications(cleanupDaysOld);
            log.info("Completed scheduled job: Cleaned up {} old notifications", deleted);
        } catch (Exception e) {
            log.error("Error in scheduled job (cleanup): {}", e.getMessage(), e);
        }
    }
}

