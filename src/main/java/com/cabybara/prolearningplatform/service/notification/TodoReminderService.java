package com.cabybara.prolearningplatform.service.notification;

public interface TodoReminderService {
    void sendDailyTodoReminders();
    void sendWeeklyTodoReminders();
    void debugSendDailyTodoReminders();
    void debugSendWeeklyTodoReminders();
}
