package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.model.noti.Notification;
import com.cabybara.prolearningplatform.service.fcm.FCMService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Override
    public Notification dispatch(CreateNotificationDto createDto) {
        Notification notification = notificationService.createNotification(createDto);

        if (createDto.isSendPush()) {
            fcmService.sendPushForNotificationAsync(notification, createDto.getUserId());
        }

        return notification;
    }

    @Override
    public List<Notification> dispatchToMany(List<CreateNotificationDto> createDtos) {
        List<Notification> notifications = notificationService.createNotifications(createDtos);

        List<CreateNotificationDto> pushEnabled = createDtos.stream()
                .filter(CreateNotificationDto::isSendPush)
                .toList();

        if (!pushEnabled.isEmpty()) {
            fcmService.sendPushForNotificationsAsync(notifications);
        }

        return notifications;
    }

    @Override
    public Notification dispatchToUser(Long userId, String title, String message, NotificationType type) {
        CreateNotificationDto createDto = CreateNotificationDto.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .sendPush(true)
                .build();

        return dispatch(createDto);
    }
}
