package com.cabybara.prolearningplatform.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponseDto {
    private List<NotificationResponseDto> notifications;
    private Long unreadCount;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
}

