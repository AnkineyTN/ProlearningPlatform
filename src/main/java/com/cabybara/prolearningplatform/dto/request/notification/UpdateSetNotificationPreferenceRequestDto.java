package com.cabybara.prolearningplatform.dto.request.notification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateSetNotificationPreferenceRequestDto {

    private Boolean weeklySummaryEnabled;

    @Min(1)
    @Max(7)
    private Integer weeklySummaryDay;
}
