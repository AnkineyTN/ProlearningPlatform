package com.cabybara.prolearningplatform.service.fcm;

import com.cabybara.prolearningplatform.dto.internal.FCMMessage;
import com.cabybara.prolearningplatform.model.noti.Notification;
import com.google.firebase.messaging.BatchResponse;

import java.util.List;

public interface FCMService {

    BatchResponse sendNotification(FCMMessage message);

    String sendSingleNotification(FCMMessage message);

    int sendBatchNotifications(List<FCMMessage> messages);

    void sendPushForNotificationAsync(Notification notification, Long userId);


    void sendPushForNotificationsAsync(List<Notification> notifications);
}

