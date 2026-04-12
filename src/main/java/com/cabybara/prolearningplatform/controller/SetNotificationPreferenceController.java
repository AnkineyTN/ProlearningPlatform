package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.service.notification.SetNotificationPreferenceService;
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
@RequestMapping("/sets/{setId}/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "Set Notification Preferences")
@PreAuthorize("isAuthenticated()")
public class SetNotificationPreferenceController {

    private final SetNotificationPreferenceService setNotificationPreferenceService;

    @GetMapping
    @Operation(
            summary = "Get notification preferences for a set",
            description = "Returns notification preferences scoped to a specific study set"
    )
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDto>> getPreferences(
            @PathVariable Long setId
    ) {
        NotificationPreferenceResponseDto response =
                setNotificationPreferenceService.getPreferenceForCurrentUser(setId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get set notification preferences successfully", response, null));
    }

    @PutMapping
    @Operation(
            summary = "Update notification preferences for a set",
            description = "Updates notification preferences scoped to a specific study set"
    )
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDto>> updatePreferences(
            @PathVariable Long setId,
            @Valid @RequestBody UpdateNotificationPreferenceRequestDto request
    ) {
        NotificationPreferenceResponseDto response =
                setNotificationPreferenceService.updatePreference(setId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update set notification preferences successfully", response, null));
    }
}
