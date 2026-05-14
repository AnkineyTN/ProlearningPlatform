package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.user.AppealRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.PublicAppealRequestDto;
import com.cabybara.prolearningplatform.dto.response.appeal.AppealResponseDto;
import com.cabybara.prolearningplatform.service.appeal.AppealService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Appeal")
public class UserAppealController {

    private final AppealService appealService;

    @Operation(summary = "Submit appeal (authenticated)", description = "For users who still have a valid session when they receive the suspension notification.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/users/me/appeal")
    public ResponseEntity<ApiResponse<AppealResponseDto>> submitAppeal(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AppealRequestDto request) {
        Long userId = Long.parseLong(jwt.getClaims().get("id").toString());
        AppealResponseDto dto = appealService.submitAppeal(userId, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Appeal submitted", dto, null));
    }

    @Operation(summary = "Submit appeal (public)", description = "For users whose session has expired. Requires only email and reason.")
    @PostMapping("/public/appeals")
    public ResponseEntity<ApiResponse<AppealResponseDto>> submitPublicAppeal(
            @Valid @RequestBody PublicAppealRequestDto request) {
        AppealResponseDto dto = appealService.submitPublicAppeal(request.getEmail(), request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Appeal submitted", dto, null));
    }
}
