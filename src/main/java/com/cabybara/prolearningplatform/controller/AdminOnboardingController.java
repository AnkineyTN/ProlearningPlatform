package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingSubmissionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/onboarding")
@RequiredArgsConstructor
@Tag(name = "Admin — Onboarding")
@SecurityRequirement(name = "bearerAuth")
public class AdminOnboardingController {

    private final OnboardingSubmissionService onboardingSubmissionService;

    @Operation(
            summary = "List of users who have onboarded (pagination)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/submissions")
    @ValidateSort(allowedFields = {"updatedAt", "id"})
    public ResponseEntity<ApiResponse<Object>> listSubmissions(
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OnboardingSubmissionResponseDto> page = onboardingSubmissionService.listAll(pageable);
        PaginationResponseDto meta = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), meta));
    }

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
