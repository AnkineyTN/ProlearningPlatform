package com.cabybara.prolearningplatform.dto.internal;

import com.cabybara.prolearningplatform.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CreateNotificationDto {
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private String actionUrl;

    @Builder.Default
    private boolean sendPush = true;
}

