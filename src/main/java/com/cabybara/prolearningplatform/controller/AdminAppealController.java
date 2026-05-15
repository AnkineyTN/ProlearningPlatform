package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.admin.AdminAppealReviewDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.appeal.AppealResponseDto;
import com.cabybara.prolearningplatform.enums.AppealStatus;
import com.cabybara.prolearningplatform.service.appeal.AppealService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/appeals")
@RequiredArgsConstructor
@Tag(name = "Admin — Appeals")
@SecurityRequirement(name = "bearerAuth")
public class AdminAppealController {

    private final AppealService appealService;

    @Operation(summary = "List appeals", description = "Pass status=PENDING/ACCEPTED/REJECTED to filter, or omit for all.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listAppeals(
            @RequestParam(required = false) AppealStatus status,
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AppealResponseDto> page = appealService.listAppeals(status, pageable);
        PaginationResponseDto meta = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), meta));
    }

    @Operation(summary = "Review an appeal", description = "Set status to ACCEPTED or REJECTED. Optionally include adminNote.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{appealId}")
    public ResponseEntity<ApiResponse<AppealResponseDto>> reviewAppeal(
            @PathVariable Long appealId,
            @Valid @RequestBody AdminAppealReviewDto dto) {
        AppealResponseDto updated = appealService.reviewAppeal(appealId, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Appeal reviewed", updated, null));
    }
}
