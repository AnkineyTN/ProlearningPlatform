package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.onboarding.OnboardingSubmissionRequestDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingSubmissionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding/submissions")
@RequiredArgsConstructor
@Tag(name = "Onboarding")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class OnboardingSubmissionController {

    private final OnboardingSubmissionService onboardingSubmissionService;

    @Operation(
            summary = "Save onboarding submission")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<OnboardingSubmissionResponseDto>> submit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OnboardingSubmissionRequestDto body) {
        Long userId = Long.parseLong(jwt.getClaims().get("id").toString());
        OnboardingSubmissionResponseDto saved = onboardingSubmissionService.submit(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Onboarding saved successfully", saved, null));
    }
}
