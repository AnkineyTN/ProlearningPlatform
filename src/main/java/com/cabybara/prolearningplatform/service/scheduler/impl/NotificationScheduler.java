package com.cabybara.prolearningplatform.service.scheduler.impl;

import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
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

    private final DueCardReminderService dueCardReminderService;
    private final NotificationService notificationService;

    @Value("${notification.cleanup.days-old:30}")
    private int cleanupDaysOld;

    @Scheduled(cron = "${notification.due-card-reminder.cron:0 0 8 * * *}")
    public void sendDueCardReminders() {
        try {
            dueCardReminderService.sendDueCardReminders();
        } catch (Exception e) {
            log.error("Error in scheduled job (due card reminders): {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${notification.evening-reminder.cron:0 0 19 * * *}")
    public void sendEveningStudyReminders() {
        try {
            dueCardReminderService.sendEveningStudyReminders();
        } catch (Exception e) {
            log.error("Error in scheduled job (due card reminders): {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${notification.cleanup.cron:0 0 2 * * SUN}")
    public void cleanupOldNotifications() {
        try {
            int deleted = notificationService.cleanupOldNotifications(cleanupDaysOld);
            log.info("Completed scheduled job: Cleaned up {} old notifications", deleted);
        } catch (Exception e) {
            log.error("Error in scheduled job (cleanup): {}", e.getMessage(), e);
        }
    }
}

