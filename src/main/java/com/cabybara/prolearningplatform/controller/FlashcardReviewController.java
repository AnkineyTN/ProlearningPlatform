package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardLearnResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardReviewService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards/review")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard Review")
public class FlashcardReviewController {
    private final FlashcardReviewService flashcardReviewService;

    @Operation(
            summary = "API used to get list of cards to learn"
    )
    @GetMapping("/{flashcardId}/learn")
    public ResponseEntity<ApiResponse<List<CardLearnResponseDto>>> getCardsToLearn(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @Valid  @RequestParam(defaultValue = "20") int limit) {

        List<CardLearnResponseDto> cards = flashcardReviewService.getCardsForReview(setId, flashcardId, limit);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", cards, null));
    }

    @Operation(
            summary = "Api to update cards status (should use for each 3-5 card)"
    )
    @PostMapping("/{flashcardId}/reviews")
    public ResponseEntity<ApiResponse<Void>> submitReviews(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @Valid @RequestBody FlashcardStudySessionSyncRequestDto flashcardStudySessionSyncRequestDto) {

        flashcardReviewService.processBatchReview(setId, flashcardId, flashcardStudySessionSyncRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }
}
