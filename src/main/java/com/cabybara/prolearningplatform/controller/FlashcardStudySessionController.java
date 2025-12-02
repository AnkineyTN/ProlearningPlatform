package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardStudySessionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/session/{sessionId}")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard Study Session")
public class FlashcardStudySessionController {


    private final FlashcardStudySessionService flashcardStudySessionService;

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FlashcardStudySessionStatusResponseDto>> checkStudySessionStatus(
            @PathVariable Long flashcardId
    ) {

        FlashcardStudySessionStatusResponseDto flashcardStudySessionStatusResponseDto = flashcardStudySessionService.checkStudySessionStatus(flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionStatusResponseDto, null));
    }

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FlashcardStudySessionStartResponseDto>> startStudySession(
            @PathVariable Long setId,
            @PathVariable Long flashcardId
    ) throws BadRequestException {

        FlashcardStudySessionStartResponseDto flashcardStudySessionStartResponseDto = flashcardStudySessionService.startOrResumeSession(setId, flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionStartResponseDto, null));
    }

    @PutMapping("/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> syncStudySessionProgress(
            @PathVariable Long sessionId,
            @Valid @RequestBody FlashcardStudySessionSyncRequestDto flashcardStudySessionSyncRequestDto
            ) throws BadRequestException {

        flashcardStudySessionService.syncSessionProgress(sessionId, flashcardStudySessionSyncRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @GetMapping("/result")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FlashcardStudySessionResultResponseDto>> getStudySessionResult(
            @PathVariable Long sessionId
    ) throws BadRequestException {

        FlashcardStudySessionResultResponseDto flashcardStudySessionResultResponseDto = flashcardStudySessionService.getSessionResult(sessionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionResultResponseDto, null));
    }
}
