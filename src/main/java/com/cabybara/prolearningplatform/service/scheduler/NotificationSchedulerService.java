package com.cabybara.prolearningplatform.service.scheduler;

public interface NotificationSchedulerService {
    void sendDueCardReminders();

//    void sendEveningStudyReminders();

    void processWeeklySummaries();

    void cleanupOldNotifications();
}
