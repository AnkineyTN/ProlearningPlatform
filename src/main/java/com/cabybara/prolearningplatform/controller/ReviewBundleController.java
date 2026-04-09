package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.service.review.ReviewBundleService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review-bundles")
@RequiredArgsConstructor
@Tag(name = "Review Bundles")
@PreAuthorize("isAuthenticated()")
public class ReviewBundleController {

    private final ReviewBundleService reviewBundleService;

    @Operation(summary = "Get review bundle", description = "Returns the list of incorrect cards in this bundle")
    @GetMapping("/{bundleId}")
    public ResponseEntity<ApiResponse<ReviewBundleResponseDto>> getBundle(
            @PathVariable Long bundleId
    ) {
        ReviewBundleResponseDto response = reviewBundleService.getBundle(bundleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", response, null));
    }

    @Operation(summary = "Generate review flashcard", description = "Calls AI to generate a new flashcard set from the incorrect cards in this bundle")
    @PostMapping("/{bundleId}/generate-flashcard")
    public ResponseEntity<ApiResponse<GenerateFlashcardByAIResponseDto>> generateFlashcard(
            @PathVariable Long bundleId
    ) {
        GenerateFlashcardByAIResponseDto response = reviewBundleService.generateFlashcard(bundleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Flashcard generation initiated", response, null));
    }

    @Operation(summary = "Generate review exam", description = "Calls AI to generate a new exam from the incorrect cards in this bundle")
    @PostMapping("/{bundleId}/generate-exam")
    public ResponseEntity<ApiResponse<GenerateExamByAIResponseDto>> generateExam(
            @PathVariable Long bundleId
    ) {
        GenerateExamByAIResponseDto response = reviewBundleService.generateExam(bundleId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Exam generation initiated", response, null));
    }
}
