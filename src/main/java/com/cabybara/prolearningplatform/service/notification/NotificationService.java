package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationListResponseDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationResponseDto;
import com.cabybara.prolearningplatform.model.noti.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(CreateNotificationDto createDto);

    List<Notification> createNotifications(List<CreateNotificationDto> createDtos);

    NotificationListResponseDto getUserNotifications(int page, int size);

    NotificationListResponseDto getUnreadNotifications(int page, int size);

    long getUnreadCount();

    NotificationResponseDto markAsRead(Long notificationId);

    int markMultipleAsRead(List<Long> notificationIds);

    int markAllAsRead();

    void deleteNotification(Long notificationId);

    int cleanupOldNotifications(int daysOld);
}

