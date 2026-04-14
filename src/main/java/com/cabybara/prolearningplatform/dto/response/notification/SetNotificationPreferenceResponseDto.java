package com.cabybara.prolearningplatform.dto.response.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetNotificationPreferenceResponseDto {
    private boolean weeklySummaryEnabled;
    private int weeklySummaryDay;
}
