package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleListItemDto;
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

import java.util.List;

@RestController
@RequestMapping("/review-bundles")
@RequiredArgsConstructor
@Tag(name = "Review Bundles")
@PreAuthorize("isAuthenticated()")
public class ReviewBundleController {

    private final ReviewBundleService reviewBundleService;

    @Operation(summary = "Get all review bundles", description = "Returns all bundles for the current user, sorted newest first")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewBundleListItemDto>>> getBundles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", reviewBundleService.getBundles(), null));
    }

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

    @Operation(summary = "Dismiss review bundle", description = "Mark the bundle as mastered — permanently deletes it")
    @DeleteMapping("/{bundleId}")
    public ResponseEntity<Void> dismissBundle(
            @PathVariable Long bundleId
    ) {
        reviewBundleService.dismissBundle(bundleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate review flashcard", description = "Calls AI to generate a new flashcard set from the incorrect cards in this bundle")
    @PostMapping("/{bundleId}/generate-flashcard")
    public ResponseEntity<ApiResponse<FlashcardResponseDto>> generateFlashcard(
            @PathVariable Long bundleId
    ) {
        FlashcardResponseDto response = reviewBundleService.generateFlashcard(bundleId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Flashcard created from review bundle", response, null));
    }

    @Operation(summary = "Generate review exam", description = "Calls AI to generate a new exam from the incorrect cards in this bundle")
    @PostMapping("/{bundleId}/generate-exam")
    public ResponseEntity<ApiResponse<ExamResponseDto>> generateExam(
            @PathVariable Long bundleId
    ) {
        ExamResponseDto response = reviewBundleService.generateExam(bundleId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Exam created from review bundle", response, null));
    }
}
