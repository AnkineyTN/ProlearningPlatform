package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import com.cabybara.prolearningplatform.service.review.ReviewBundleService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * DEV ONLY — for quickly testing review bundle flows without waiting for the scheduler.
 * Active only on 'dev' profile.
 */
@RestController
@RequestMapping("/dev/review")
@RequiredArgsConstructor
@Tag(name = "[Dev] Review")
@Profile("dev")
@PreAuthorize("isAuthenticated()")
public class ReviewDevController {

    private final WeeklySummaryService weeklySummaryService;
    private final ReviewBundleService reviewBundleService;
    private final AuthenticationContext authenticationContext;

    /**
     * Trigger the full weekly summary flow for the current user.
     * Creates ReviewBundle + dispatches notification exactly like the real scheduler.
     */
    @PostMapping("/trigger-summary")
    public ResponseEntity<ApiResponse<String>> triggerSummary() {
        Long userId = authenticationContext.getCurrentUserId();
        weeklySummaryService.sendWeeklySummaryToUser(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Weekly summary triggered for userId=" + userId, null, null));
    }

    /**
     * Body: { "setId": 1, "cardIds": [1, 2, 3] }
     */
    @PostMapping("/bundles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBundle(
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authenticationContext.getCurrentUserId();

        @SuppressWarnings("unchecked")
        List<Long> cardIds = ((List<Number>) body.get("cardIds")).stream()
                .map(Number::longValue)
                .toList();
        Long setId = body.get("setId") != null
                ? ((Number) body.get("setId")).longValue()
                : null;

        OffsetDateTime now = OffsetDateTime.now();
        ReviewBundle bundle = reviewBundleService.createBundle(
                userId,
                setId,
                cardIds,
                now.minusWeeks(1),
                now,
                now.plusWeeks(1)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Bundle created",
                        Map.of("bundleId", bundle.getId()), null));
    }

    /**
     * View a bundle by ID (same as the real endpoint, duplicated here for convenience).
     */
    @GetMapping("/bundles/{bundleId}")
    public ResponseEntity<ApiResponse<ReviewBundleResponseDto>> getBundle(
            @PathVariable Long bundleId
    ) {
        ReviewBundleResponseDto response = reviewBundleService.getBundle(bundleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", response, null));
    }
}
