package com.cabybara.prolearningplatform.scheduler;

import com.cabybara.prolearningplatform.service.notification.DueCardReminderService;
import com.cabybara.prolearningplatform.service.notification.GoalReminderService;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import com.cabybara.prolearningplatform.service.notification.TodoReminderService;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private static final String SCHEDULER_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private final DueCardReminderService dueCardReminderService;
    private final NotificationService notificationService;
    private final WeeklySummaryService weeklySummaryService;
    private final TodoReminderService todoReminderService;
    private final GoalReminderService goalReminderService;

    @Value("${schedule.notification-cleanup.days-old:30}")
    private int cleanupDaysOld;

    // @Scheduled(cron = "${schedule.due-card-reminder.cron:0 0 8 * * *}", zone = SCHEDULER_TIME_ZONE)
    public void sendDueCardReminders() {
        log.info("Starting due card reminder scheduler");
        try {
            dueCardReminderService.sendDueCardReminders();
        } catch (Exception e) {
            log.error("Error in scheduled job (due card reminders): {}", e.getMessage(), e);
        }
        log.info("Done due card reminder scheduler");
    }

    @Scheduled(cron = "${schedule.weekly-summary.cron:0 0 9 * * *}", zone = SCHEDULER_TIME_ZONE)
    public void processWeeklySummaries() {
        log.info("Starting weekly process scheduler");
        try {
            weeklySummaryService.processWeeklySummaries();
        } catch (Exception e) {
            log.error("Error in scheduled job (weekly summaries): {}", e.getMessage(), e);
        }
        log.info("Done weekly process scheduler");
    }

    @Scheduled(cron = "${schedule.todo-goal-reminder.cron:0 0 * * * *}", zone = SCHEDULER_TIME_ZONE)
    public void sendTodoAndGoalReminders() {
        log.info("Starting todo and goal reminder scheduler");
        try {
            todoReminderService.sendDailyTodoReminders();
        } catch (Exception e) {
            log.error("Error in daily todo reminders: {}", e.getMessage(), e);
        }
        try {
            todoReminderService.sendWeeklyTodoReminders();
        } catch (Exception e) {
            log.error("Error in weekly todo reminders: {}", e.getMessage(), e);
        }
        try {
            goalReminderService.sendGoalDeadlineReminders();
        } catch (Exception e) {
            log.error("Error in goal deadline reminders: {}", e.getMessage(), e);
        }
        try {
            goalReminderService.sendGoalInactiveReminders();
        } catch (Exception e) {
            log.error("Error in goal inactive reminders: {}", e.getMessage(), e);
        }
        log.info("Done todo and goal reminder scheduler");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runTodoAndGoalReminderCatchupOnStartup() {
        log.info("Running todo/goal reminder catch-up at startup");
        sendTodoAndGoalReminders();
    }

    @Scheduled(cron = "${schedule.notification-cleanup.cron:0 0 2 * * SUN}", zone = SCHEDULER_TIME_ZONE)
    public void cleanupOldNotifications() {
        try {
            int deleted = notificationService.cleanupOldNotifications(cleanupDaysOld);
            log.info("Completed scheduled job: Cleaned up {} old notifications", deleted);
        } catch (Exception e) {
            log.error("Error in scheduled job (cleanup): {}", e.getMessage(), e);
        }
    }
}
