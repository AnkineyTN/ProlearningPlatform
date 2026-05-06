package com.cabybara.prolearningplatform.dto.request.notification;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MarkNotificationsReadRequestDto {
    @NotEmpty(message = "Notification IDs list cannot be empty")
    private List<Long> notificationIds;
}

