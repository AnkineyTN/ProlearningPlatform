package com.cabybara.prolearningplatform.service.notification;

public interface DueCardReminderService {

    void sendDueCardReminders();

    void sendEveningStudyReminders();

    void sendDueCardReminderToUser(Long userId, long dueCount);
}

