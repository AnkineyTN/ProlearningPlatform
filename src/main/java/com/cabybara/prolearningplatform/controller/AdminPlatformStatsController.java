package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.admin.AdminPlatformStatsResponseDto;
import com.cabybara.prolearningplatform.service.admin.AdminPlatformStatsService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin — Platform Stats", description = "Aggregate platform statistics for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AdminPlatformStatsController {

    private final AdminPlatformStatsService adminPlatformStatsService;

    @Operation(summary = "Platform statistics", description = "Returns aggregate counts: users, notes, flashcards, exams, sessions.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminPlatformStatsResponseDto>> getStats() {
        AdminPlatformStatsResponseDto stats = adminPlatformStatsService.getStats();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", stats, null));
    }
}
