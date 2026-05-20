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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Long, List<Notification>> pushByUser = new HashMap<>();
        for (int i = 0; i < createDtos.size(); i++) {
            if (createDtos.get(i).isSendPush()) {
                pushByUser.computeIfAbsent(createDtos.get(i).getUserId(), k -> new ArrayList<>())
                          .add(notifications.get(i));
            }
        }

        if (!pushByUser.isEmpty()) {
            fcmService.sendPushForNotificationsAsync(pushByUser);
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
