package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.internal.FCMMessage;
import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.DeviceTokenRegistrationDto;
import com.cabybara.prolearningplatform.dto.request.notification.MarkNotificationsReadRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationListResponseDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationResponseDto;
import com.cabybara.prolearningplatform.service.fcm.DeviceTokenService;
import com.cabybara.prolearningplatform.service.fcm.FCMService;
import com.cabybara.prolearningplatform.service.notification.NotificationPreferenceService;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.mapper.NotificationMapper;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final DeviceTokenService deviceTokenService;
    private final FCMService fcmService;
    private final WeeklySummaryService weeklySummaryService;
    private final NotificationDispatcher notificationDispatcher;
    private final NotificationMapper notificationMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get user notifications",
            description = "Retrieve all notifications for the current user with pagination"
    )
    public ResponseEntity<ApiResponse<NotificationListResponseDto>> getUserNotifications(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationListResponseDto response = notificationService.getUserNotifications(page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Notifications retrieved successfully", response, null));
    }

    @Operation(
            summary = "Get current user notification preferences",
            description = "Retrieve notification preferences of the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDto>> getPreferences() {
        NotificationPreferenceResponseDto response =
                notificationPreferenceService.getCurrentUserPreference();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get user notification preference successfully", response, null));
    }

    @Operation(
            summary = "Update current user notification preferences",
            description = "Update notification preferences for the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePreferences(
            @Valid @RequestBody UpdateNotificationPreferenceRequestDto request) {
        NotificationPreferenceResponseDto response =
                notificationPreferenceService.updateCurrentUserPreference(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update user notification preference successfully", response, null));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get unread notifications",
            description = "Retrieve only unread notifications for the current user"
    )
    public ResponseEntity<ApiResponse<NotificationListResponseDto>> getUnreadNotifications(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationListResponseDto response = notificationService.getUnreadNotifications(page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Unread notifications retrieved successfully", response, null));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get unread notification count",
            description = "Get the count of unread notifications for the current user"
    )
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Unread count retrieved", Map.of("unreadCount", count), null));
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a single notification as read"
    )
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(
            @PathVariable Long notificationId
    ) {
        NotificationResponseDto response = notificationService.markAsRead(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Notification marked as read", response, null));
    }

    @PatchMapping("/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark multiple notifications as read",
            description = "Mark multiple notifications as read by their IDs"
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markMultipleAsRead(
            @Valid @RequestBody MarkNotificationsReadRequestDto request
    ) {
        int count = notificationService.markMultipleAsRead(request.getNotificationIds());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Notifications marked as read", Map.of("markedCount", count), null));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the current user"
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("All notifications marked as read", Map.of("markedCount", count), null));
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Delete notification",
            description = "Delete a single notification"
    )
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId
    ) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Notification deleted successfully", null, null));
    }


    @PostMapping("/device/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Register device token",
            description = "Save or update a device token for push notifications"
    )
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestBody @Valid DeviceTokenRegistrationDto request
    ) {
        deviceTokenService.saveOrUpdateToken(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Device registered successfully", null, null));
    }

    @PostMapping("/device/unregister")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Unregister device token",
            description = "Remove a device token so the device will no longer receive push notifications"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "JSON object with a single field `token`",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"token\":\"string\"}")
            )
    )
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @RequestBody Map<String, String> request
    ) {
        deviceTokenService.removeToken(request.get("token"));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Device unregistered successfully", null, null));
    }

    @Hidden
    @PostMapping("/debug/create/{userId}")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> debugCreateNotification(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String typeStr = body != null ? body.get("type") : null;
        NotificationType type = typeStr != null
                ? NotificationType.valueOf(typeStr)
                : NotificationType.GENERAL;

        String title   = (body != null && body.get("title")   != null) ? body.get("title")   : type.getDefaultTitle();
        String message = (body != null && body.get("message") != null) ? body.get("message") : "Debug notification";

        var notification = notificationDispatcher.dispatchToUser(userId, title, message, type);
        return ResponseEntity.ok(ResponseUtil.success("Debug notification created",
                notificationMapper.toResponseDto(notification), null));
    }

    @Hidden
    @PostMapping("/debug/weekly-summary/process")
    public ResponseEntity<ApiResponse<Map<String, String>>> debugProcessWeeklySummaries() {
        weeklySummaryService.processWeeklySummaries();
        return ResponseEntity.ok(ResponseUtil.success(
                "Weekly summary processing triggered for today's scheduled users", null, null));
    }

    @Hidden
    @PostMapping("/debug/weekly-summary/send/{userId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> debugSendWeeklySummary(
            @PathVariable Long userId) {
        weeklySummaryService.sendWeeklySummaryToUser(userId);
        return ResponseEntity.ok(ResponseUtil.success(
                "Weekly summary sent to user " + userId, null, null));
    }

    @Hidden
    @PostMapping("/test-push")
    public ResponseEntity<Object> testPush(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        FCMMessage fcmMessage = FCMMessage.builder()
                .subject("Test Title")
                .content("Test Body Success!")
                .registrationTokens(List.of(token))
                .build();

        try {
            String messageId = fcmService.sendSingleNotification(fcmMessage);
            if (messageId != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Push notification sent successfully!",
                        "messageId", messageId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Failed to send push notification"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}

