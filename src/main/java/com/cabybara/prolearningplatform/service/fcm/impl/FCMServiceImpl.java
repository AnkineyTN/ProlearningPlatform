package com.cabybara.prolearningplatform.service.fcm.impl;

import com.cabybara.prolearningplatform.dto.internal.FCMMessage;
import com.cabybara.prolearningplatform.model.Notification;
import com.cabybara.prolearningplatform.repository.DeviceTokenRepository;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.service.fcm.FCMService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMServiceImpl implements FCMService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public BatchResponse sendNotification(FCMMessage message) {
        if (!message.hasValidTokens()) {
            log.debug("No valid tokens provided for FCM message");
            return null;
        }

        List<String> registrationTokens = message.getRegistrationTokens();

        com.google.firebase.messaging.Notification.Builder notificationBuilder =
                com.google.firebase.messaging.Notification.builder()
                .setTitle(message.getSubject())
                .setBody(message.getContent());

        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            notificationBuilder.setImage(message.getImageUrl());
        }

        MulticastMessage.Builder multicastBuilder = MulticastMessage.builder()
                .addAllTokens(registrationTokens)
                .setNotification(notificationBuilder.build());

        if (message.getData() != null && !message.getData().isEmpty()) {
            multicastBuilder.putAllData(message.getData());
        }

        MulticastMessage multicastMessage = multicastBuilder.build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendMulticast(multicastMessage);
            log.info("FCM multicast sent: {} success, {} failure",
                    batchResponse.getSuccessCount(), batchResponse.getFailureCount());

            if (batchResponse.getFailureCount() > 0) {
                handleFcmTokenFailure(registrationTokens, batchResponse);
            }

            return batchResponse;
        } catch (FirebaseMessagingException e) {
            log.error("Firebase messaging error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String sendSingleNotification(FCMMessage message) {
        if (!message.hasValidTokens()) {
            log.debug("No valid token provided for single FCM message");
            return null;
        }

        String token = message.getRegistrationTokens().getFirst();

        com.google.firebase.messaging.Notification.Builder notificationBuilder =
                com.google.firebase.messaging.Notification.builder()
                .setTitle(message.getSubject())
                .setBody(message.getContent());

        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            notificationBuilder.setImage(message.getImageUrl());
        }

        Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(notificationBuilder.build());

        if (message.getData() != null && !message.getData().isEmpty()) {
            message.getData().forEach(messageBuilder::putData);
        }

        try {
            String messageId = firebaseMessaging.send(messageBuilder.build());
            log.debug("FCM single message sent successfully: {}", messageId);
            return messageId;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send single FCM message: {}", e.getMessage());
            handleSingleTokenFailure(token, e);
            return null;
        }
    }

    @Override
    public int sendBatchNotifications(List<FCMMessage> messages) {
        int totalSuccess = 0;

        for (FCMMessage message : messages) {
            BatchResponse response = sendNotification(message);
            if (response != null) {
                totalSuccess += response.getSuccessCount();
            }
        }

        log.info("Batch notifications completed: {} total successful", totalSuccess);
        return totalSuccess;
    }

    @Override
    @Async
    public void sendPushForNotificationAsync(Notification notification, Long userId) {
        try {
            List<String> tokens = deviceTokenRepository.findAllTokensByUserId(userId);

            if (tokens.isEmpty()) {
                log.debug("No device tokens found for user {}", userId);
                return;
            }

            FCMMessage fcmMessage = buildFCMMessageFromNotification(notification, tokens);
            BatchResponse response = sendNotification(fcmMessage);

            if (response != null && response.getSuccessCount() > 0) {
                notification.setPushSent(true);
                notificationRepository.save(notification);
            }

        } catch (Exception e) {
            log.error("Error sending push for notification {}: {}", notification.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPushForNotificationsAsync(List<Notification> notifications) {
        Map<Long, List<Notification>> notificationsByUser = notifications.stream()
                .collect(Collectors.groupingBy(n -> n.getUser().getId()));

        for (Map.Entry<Long, List<Notification>> entry : notificationsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Notification> userNotifications = entry.getValue();

            try {
                List<String> tokens = deviceTokenRepository.findAllTokensByUserId(userId);
                if (tokens.isEmpty()) {
                    continue;
                }

                for (Notification notification : userNotifications) {
                    FCMMessage fcmMessage = buildFCMMessageFromNotification(notification, tokens);
                    BatchResponse response = sendNotification(fcmMessage);

                    if (response != null && response.getSuccessCount() > 0) {
                        notification.setPushSent(true);
                    }
                }
            } catch (Exception e) {
                log.error("Error sending batch push to user {}: {}", userId, e.getMessage());
            }
        }

        notificationRepository.saveAll(notifications);
    }

    private FCMMessage buildFCMMessageFromNotification(Notification notification, List<String> tokens) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notification.getId().toString());
        data.put("type", notification.getType().name());

        if (notification.getActionUrl() != null) {
            data.put("actionUrl", notification.getActionUrl());
        }

        if (notification.getData() != null) {
            notification.getData().forEach((key, value) -> {
                if (value != null) {
                    data.put(key, value.toString());
                }
            });
        }

        return FCMMessage.builder()
                .subject(notification.getTitle())
                .content(notification.getMessage())
                .data(data)
                .registrationTokens(tokens)
                .build();
    }

    private void handleFcmTokenFailure(List<String> tokens, BatchResponse batchResponse) {
        List<SendResponse> responses = batchResponse.getResponses();
        List<String> failedTokens = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (!response.isSuccessful()) {
                FirebaseMessagingException exception = response.getException();
                if (exception != null && isInvalidTokenError(exception)) {
                    failedTokens.add(tokens.get(i));
                    log.warn("Invalid FCM token will be removed: {}...",
                            tokens.get(i).substring(0, Math.min(10, tokens.get(i).length())));
                }
            }
        }

        if (!failedTokens.isEmpty()) {
            deviceTokenRepository.deleteAllByTokenIn(failedTokens);
            log.info("Removed {} invalid FCM tokens from database", failedTokens.size());
        }
    }

    private void handleSingleTokenFailure(String token, FirebaseMessagingException e) {
        if (isInvalidTokenError(e)) {
            deviceTokenRepository.deleteByToken(token);
            log.info("Removed invalid FCM token from database: {}...",
                    token.substring(0, Math.min(10, token.length())));
        }
    }

    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
               errorCode == MessagingErrorCode.UNREGISTERED;
    }
}

