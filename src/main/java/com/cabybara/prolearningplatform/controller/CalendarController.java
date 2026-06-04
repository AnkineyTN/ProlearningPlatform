package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.calendar.CalendarStatusResponse;
import com.cabybara.prolearningplatform.service.calendar.CalendarService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequestMapping("/calendar")
@Tag(name = "Google Calendar APIs")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "Get Google Calendar connection status")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CalendarStatusResponse>> getStatus() {
        return ResponseEntity.ok(ResponseUtil.success("Successfully", calendarService.getStatus(), null));
    }

    @Operation(summary = "Get Google OAuth authorization URL to connect Google Calendar")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/auth/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthorizationUrl() throws IOException {
        String url = calendarService.getAuthorizationUrl();
        return ResponseEntity.ok(ResponseUtil.success("Successfully", Map.of("authorizationUrl", url), null));
    }

    @Operation(summary = "OAuth callback from Google (handled by backend, redirects to frontend)")
    @GetMapping("/auth/callback")
    public void handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse response
    ) throws IOException, GeneralSecurityException {
        calendarService.handleCalendarCallback(code, state, response);
    }

    @Operation(summary = "Toggle calendar sync on/off")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Object>> updateSettings(@RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("syncEnabled");
        if (enabled == null) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.success("Missing 'syncEnabled' field", null, null));
        }
        calendarService.updateSyncEnabled(enabled);
        return ResponseEntity.ok(ResponseUtil.success("Calendar sync updated", null, null));
    }

    @Operation(summary = "Disconnect Google Calendar")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/disconnect")
    public ResponseEntity<ApiResponse<Object>> disconnect() {
        calendarService.disconnectCalendar();
        return ResponseEntity.ok(ResponseUtil.success("Google Calendar disconnected", null, null));
    }
}
