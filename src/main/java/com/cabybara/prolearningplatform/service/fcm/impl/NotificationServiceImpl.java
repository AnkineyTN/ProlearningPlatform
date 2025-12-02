package com.cabybara.prolearningplatform.service.fcm.impl;

import com.cabybara.prolearningplatform.model.fcm.FCMMessage;
import com.cabybara.prolearningplatform.repository.DeviceTokenRepository;
import com.cabybara.prolearningplatform.service.fcm.NotificationService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public BatchResponse sendNotification(FCMMessage message) {
        List<String> registrationTokens = message.getRegistrationTokens();
        Notification notification = Notification.builder()
                .setTitle(message.getSubject())
                .setBody(message.getContent())
                .build();

        MulticastMessage multicastMessage = MulticastMessage.builder()
                .addAllTokens(registrationTokens)
                .setNotification(notification)
                .putAllData(message.getData())
                .build();

        BatchResponse batchResponse = null;
        try {
            batchResponse = firebaseMessaging.sendMulticast(multicastMessage);
        } catch (FirebaseMessagingException e) {
            log.info("Firebase error {}", e.getMessage());
        }

        if (batchResponse.getFailureCount() > 0) {
            handleFcmTokenFailure(registrationTokens, batchResponse);
        }
        return batchResponse;
    }

    @Override
    @Async
    public void sendPersonalizedNotification(Long userId, Long dueCount) {
        List<String> tokens = deviceTokenRepository.findAllTokensByUserId(userId);

        if (tokens.isEmpty()) return;

        String title = "Time to revision!";
        String body = "You have " + dueCount + " card is due. Review now!";

        Map<String, String> data = new HashMap<>();
        data.put("type", "STUDY_REMINDER");
        data.put("cardCount", String.valueOf(dueCount));

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .putAllData(data)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendMulticast(message);
            log.info("Sent to User {}: {} success, {} failed", userId, batchResponse.getSuccessCount(), batchResponse.getFailureCount());

            handleFcmTokenFailure(tokens, batchResponse);

        } catch (FirebaseMessagingException e) {
            log.error("Firebase error for user {}", userId, e);
        }
    }

    private void handleFcmTokenFailure(List<String> fcmsTokens, BatchResponse batchResponse) {
        List<SendResponse> responses = batchResponse.getResponses();
        List<String> failedTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                failedTokens.add(fcmsTokens.get(i));
            }
        }
        log.info("List of tokens that caused failures: " + failedTokens);

        deviceTokenRepository.deleteAllByTokenIn(failedTokens);
    }
}

