package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateUserNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.UserNotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.service.notification.UserNotificationPreferenceService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "User Notification Preferences")
@PreAuthorize("isAuthenticated()")
public class UserNotificationPreferenceController {

    private final UserNotificationPreferenceService userNotificationPreferenceService;

    @GetMapping
    @Operation(
            summary = "Get user notification preferences",
            description = "Returns the current user's global notification preferences"
    )
    public ResponseEntity<ApiResponse<UserNotificationPreferenceResponseDto>> getPreferences() {
        UserNotificationPreferenceResponseDto response =
                userNotificationPreferenceService.getPreferenceForCurrentUser();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get user notification preferences successfully", response, null));
    }

    @PutMapping
    @Operation(
            summary = "Update user notification preferences",
            description = "Updates the current user's global notification preferences"
    )
    public ResponseEntity<ApiResponse<UserNotificationPreferenceResponseDto>> updatePreferences(
            @Valid @RequestBody UpdateUserNotificationPreferenceRequestDto request
    ) {
        UserNotificationPreferenceResponseDto response =
                userNotificationPreferenceService.updatePreference(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update user notification preferences successfully", response, null));
    }
}
