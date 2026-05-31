package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.roadmap.AcceptRoadmapRequestDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapDetailResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicCompleteResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicStartResponseDto;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roadmaps")
@Tag(name = "Roadmap APIs")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "Generate a roadmap preview from AI (stateless)")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<RoadmapPreviewResponseDto>> previewRoadmap(
            @Valid @RequestBody RoadmapPreviewRequestDto request
    ) {
        RoadmapPreviewResponseDto preview = roadmapService.previewRoadmap(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Roadmap preview generated", preview, null));
    }

    @Operation(summary = "Accept a roadmap (client may have edited the preview) and persist it")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @PostMapping("")
    public ResponseEntity<ApiResponse<RoadmapDetailResponseDto>> acceptRoadmap(
            @Valid @RequestBody AcceptRoadmapRequestDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        RoadmapDetailResponseDto response = roadmapService.acceptRoadmap(userId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Roadmap created successfully", response, null));
    }

    @Operation(summary = "List all roadmaps of the current user with progress")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Object>> getRoadmaps(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        Page<RoadmapListItemResponseDto> page = roadmapService.getRoadmaps(userId, pageable);

        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), pagination));
    }

    @Operation(summary = "Get roadmap detail with chapter and topic progress")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @GetMapping("/{roadmapId}")
    public ResponseEntity<ApiResponse<RoadmapDetailResponseDto>> getRoadmap(
            @PathVariable Long roadmapId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        RoadmapDetailResponseDto response = roadmapService.getRoadmap(userId, roadmapId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", response, null));
    }

    @Operation(summary = "Start learning a topic: adds a Note to the roadmap's Set and triggers async note generation")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @PostMapping("/{roadmapId}/topics/{topicId}/start")
    public ResponseEntity<ApiResponse<TopicStartResponseDto>> startTopic(
            @PathVariable Long roadmapId,
            @PathVariable Long topicId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        TopicStartResponseDto response = roadmapService.startTopic(userId, roadmapId, topicId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Topic started", response, null));
    }

    @Operation(summary = "Mark a topic as done; auto-completes chapter and unlocks the next one")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @PatchMapping("/{roadmapId}/topics/{topicId}/complete")
    public ResponseEntity<ApiResponse<TopicCompleteResponseDto>> markTopicComplete(
            @PathVariable Long roadmapId,
            @PathVariable Long topicId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        TopicCompleteResponseDto response = roadmapService.markTopicComplete(userId, roadmapId, topicId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Topic marked as complete", response, null));
    }

    @Operation(summary = "Abandon a roadmap (sets status to ABANDONED; Sets and Notes are kept)")
    @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
    @DeleteMapping("/{roadmapId}")
    public ResponseEntity<ApiResponse<Object>> abandonRoadmap(
            @PathVariable Long roadmapId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = (Long) jwt.getClaims().get("id");
        roadmapService.abandonRoadmap(userId, roadmapId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Roadmap abandoned", null, null));
    }
}
