package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.DeviceTokenRegistrationDto;
import com.cabybara.prolearningplatform.service.fcm.DeviceTokenService;
import com.cabybara.prolearningplatform.service.fcm.NotificationService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notification")
@Validated
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    @Hidden
    @PostMapping("/test-send")
    public ResponseEntity<Object> testSend(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("Test Title")
                        .setBody("Test Body Success!")
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            return ResponseEntity.ok("Sent!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestBody @Valid DeviceTokenRegistrationDto request
    ) {

        deviceTokenService.saveOrUpdateToken(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @RequestBody Map<String, String> request) {

        deviceTokenService.removeToken(request.get("token"));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }
}
