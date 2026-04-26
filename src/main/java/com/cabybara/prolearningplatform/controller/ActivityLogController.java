package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.activity.ActivityLogRequestDto;
import com.cabybara.prolearningplatform.dto.response.activity.ActivitySummaryResponseDto;
import com.cabybara.prolearningplatform.dto.response.activity.HeatmapDayDto;
import com.cabybara.prolearningplatform.dto.response.activity.StreakResponseDto;
import com.cabybara.prolearningplatform.service.activity.ActivityLogService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity-log")
@RequiredArgsConstructor
@Validated
@Tag(name = "Activity Log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log a learning session")
    public ResponseEntity<ApiResponse<Object>> logActivity(
            @Valid @RequestBody ActivityLogRequestDto dto) {
        activityLogService.logActivity(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Activity logged", null, null));
    }

    @GetMapping("/heatmap")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get heatmap data for the past N months")
    public ResponseEntity<ApiResponse<List<HeatmapDayDto>>> getHeatmap(
            @RequestParam(defaultValue = "6") int months) {
        List<HeatmapDayDto> data = activityLogService.getHeatmap(months);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("OK", data, null));
    }

    @GetMapping("/streak")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current and longest learning streak")
    public ResponseEntity<ApiResponse<StreakResponseDto>> getStreak() {
        StreakResponseDto data = activityLogService.getStreak();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("OK", data, null));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get activity summary for the past N days")
    public ResponseEntity<ApiResponse<ActivitySummaryResponseDto>> getSummary(
            @RequestParam(defaultValue = "30") int days) {
        ActivitySummaryResponseDto data = activityLogService.getSummary(days);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("OK", data, null));
    }
}
