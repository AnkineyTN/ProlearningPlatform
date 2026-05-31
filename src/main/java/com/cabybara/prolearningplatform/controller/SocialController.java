package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.ResourceType;
import com.cabybara.prolearningplatform.enums.TrendingPeriod;
import com.cabybara.prolearningplatform.service.social.ResourceViewLogService;
import com.cabybara.prolearningplatform.service.social.SocialService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
@Tag(name = "Social")
@Validated
public class SocialController {

    private final SocialService socialService;
    private final ResourceViewLogService resourceViewLogService;
    private final AuthenticationContext authenticationContext;

    @GetMapping("/notes")
    @Operation(summary = "Browse public notes")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicNotes(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialNotes(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/flashcards")
    @Operation(summary = "Browse public flashcards")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicFlashcards(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialFlashcards(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/exams")
    @Operation(summary = "Browse public exams")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicExams(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialExams(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/trending/resources")
    @Operation(summary = "Get trending public resources")
    public ResponseEntity<ApiResponse<?>> getTrendingResources(
            @RequestParam(defaultValue = "D7") TrendingPeriod period,
            @RequestParam(defaultValue = "10") int top,
            @RequestParam(defaultValue = "ALL") ResourceType type
    ) {
        int limit = Math.min(Math.max(top, 1), 50);
        var data = socialService.getTrendingResources(period, limit, type);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", data, null));
    }

    @GetMapping("/trending/creators")
    @Operation(summary = "Get top creators")
    public ResponseEntity<ApiResponse<?>> getTopCreators(
            @RequestParam(defaultValue = "D7") TrendingPeriod period,
            @RequestParam(defaultValue = "10") int top
    ) {
        int limit = Math.min(Math.max(top, 1), 50);
        var data = socialService.getTopCreators(period, limit);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", data, null));
    }

    @GetMapping("/trending/topics")
    @Operation(summary = "Get top trending topics")
    public ResponseEntity<ApiResponse<?>> getTopTopics(
            @RequestParam(defaultValue = "D7") TrendingPeriod period,
            @RequestParam(defaultValue = "10") int top
    ) {
        int limit = Math.min(Math.max(top, 1), 50);
        var data = socialService.getTopTopics(period, limit);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", data, null));
    }

    @PostMapping("/resources/{type}/{id}/view")
    @Operation(summary = "Record a view event for a resource")
    public ResponseEntity<ApiResponse<?>> recordResourceView(
            @PathVariable ContentType type,
            @PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        String ip = getClientIp(request);
        Long userId = null;
        try {
            userId = authenticationContext.getCurrentUserId();
        } catch (Exception e) {
            // Ignore guest user exceptions
        }
        resourceViewLogService.recordView(id, type, ip, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("View recorded", null, null));
    }

    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private PaginationResponseDto buildPagination(Page<?> page) {
        return PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
