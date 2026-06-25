package com.cabybara.prolearningplatform.service.notification;

public interface GoalReminderService {
    void sendGoalDeadlineReminders();
    void sendGoalInactiveReminders();
    void debugSendGoalDeadlineReminders();
    void debugSendGoalInactiveReminders();
}
