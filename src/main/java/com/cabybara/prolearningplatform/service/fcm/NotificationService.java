package com.cabybara.prolearningplatform.service.fcm;

import com.cabybara.prolearningplatform.model.fcm.FCMMessage;
import com.google.firebase.messaging.BatchResponse;

public interface NotificationService {
    BatchResponse sendNotification(FCMMessage message);

    void sendPersonalizedNotification(Long userId, Long dueCount);
}
