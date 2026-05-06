package com.cabybara.prolearningplatform.dto.response.notification;

import com.cabybara.prolearningplatform.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponseDto {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private String actionUrl;
    private Boolean isRead;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;
}

