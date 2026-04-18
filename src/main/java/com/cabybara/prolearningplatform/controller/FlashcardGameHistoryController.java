package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardGameResultRequest;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameHistoryResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameRankingEntryDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardGameHistoryService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/game")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard Game History")
public class FlashcardGameHistoryController {

    private final FlashcardGameHistoryService flashcardGameHistoryService;

    @PostMapping("/results")
    @PreAuthorize("isAuthenticated() and @flashcardPermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #flashcardId)")
    @Operation(summary = "Save game result", description = "Save a completed matching game result for the current user")
    public ResponseEntity<ApiResponse<FlashcardGameHistoryResponseDto>> saveResult(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @Valid @RequestBody FlashcardGameResultRequest request
    ) {
        FlashcardGameHistoryResponseDto result = flashcardGameHistoryService.saveResult(flashcardId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Game result saved successfully", result, null));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated() and @flashcardPermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #flashcardId)")
    @Operation(summary = "Get user game history", description = "Return the current user's game history for this flashcard, newest first")
    public ResponseEntity<ApiResponse<List<FlashcardGameHistoryResponseDto>>> getUserHistory(
            @PathVariable Long setId,
            @PathVariable Long flashcardId
    ) {
        List<FlashcardGameHistoryResponseDto> history = flashcardGameHistoryService.getUserHistory(flashcardId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", history, null));
    }

    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated() and @flashcardPermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #flashcardId)")
    @Operation(summary = "Get game ranking", description = "Return top 20 players ranked by fastest completion time")
    public ResponseEntity<ApiResponse<List<FlashcardGameRankingEntryDto>>> getRanking(
            @PathVariable Long setId,
            @PathVariable Long flashcardId
    ) {
        List<FlashcardGameRankingEntryDto> ranking = flashcardGameHistoryService.getRanking(flashcardId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", ranking, null));
    }
}
