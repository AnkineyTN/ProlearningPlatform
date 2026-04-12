package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingSubmissionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/onboarding")
@RequiredArgsConstructor
@Tag(name = "Admin — Onboarding", description = "Aggregated onboarding analytics. Paginated user + onboarding rows are exposed under GET /admin/users.")
@SecurityRequirement(name = "bearerAuth")
public class AdminOnboardingController {

    private final OnboardingSubmissionService onboardingSubmissionService;

    @Operation(
            summary = "Dashboard analytics",
            description = "totalRegisteredUsers; pie education; bar hearAppFrom; premium (PRO/FREE).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<OnboardingAnalyticsResponseDto>> analytics() {
        OnboardingAnalyticsResponseDto data = onboardingSubmissionService.getAnalytics();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", data, null));
    }
}
