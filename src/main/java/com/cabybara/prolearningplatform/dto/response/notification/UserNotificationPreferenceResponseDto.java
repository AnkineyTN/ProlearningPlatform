package com.cabybara.prolearningplatform.dto.response.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationPreferenceResponseDto {
    private boolean dueCardReminderEnabled;
    private boolean systemAnnouncementEnabled;
    private boolean accountActivityEnabled;

    private boolean dailyTodoReminderEnabled;
    private int dailyTodoReminderHour;

    private boolean weeklyTodoReminderEnabled;
    private int weeklyTodoReminderHour;

    private boolean goalDeadlineReminderEnabled;
    private boolean goalInactiveReminderEnabled;
    private int goalReminderHour;
}
