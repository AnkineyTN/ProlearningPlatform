package com.cabybara.prolearningplatform.dto.request.notification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateUserNotificationPreferenceRequestDto {

    private Boolean dueCardReminderEnabled;
    private Boolean systemAnnouncementEnabled;
    private Boolean accountActivityEnabled;

    private Boolean dailyTodoReminderEnabled;

    @Min(0) @Max(23)
    private Integer dailyTodoReminderHour;

    private Boolean weeklyTodoReminderEnabled;

    @Min(0) @Max(23)
    private Integer weeklyTodoReminderHour;

    private Boolean goalDeadlineReminderEnabled;
    private Boolean goalInactiveReminderEnabled;

    @Min(0) @Max(23)
    private Integer goalReminderHour;
}
