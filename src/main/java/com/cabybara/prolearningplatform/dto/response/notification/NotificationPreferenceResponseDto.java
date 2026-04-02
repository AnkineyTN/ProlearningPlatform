package com.cabybara.prolearningplatform.dto.response.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceResponseDto {
    private boolean dueCardReminderEnabled;
    private boolean weeklySummaryEnabled;
    private int weeklySummaryDay;
}