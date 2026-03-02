package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardStudySessionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/session")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard Study Session")
public class FlashcardStudySessionController {


    private final FlashcardStudySessionService flashcardStudySessionService;

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Check flashcard study session status",
            description = "Return the current status of the study session for the flashcard"
    )
    public ResponseEntity<ApiResponse<Object>> checkStudySessionStatus(
            @PathVariable(name = "setId") Long setId,
            @PathVariable(name = "flashcardId") Long flashcardId
    ) {

        List<FlashcardStudySessionStatusResponseDto> flashcardStudySessionStatusResponseDto = flashcardStudySessionService.checkStudySessionStatus(setId, flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionStatusResponseDto, null));
    }

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Start or resume flashcard study session",
            description = "Create or resume a study session for the specified flashcard in the set"
    )
    @Parameter(
            name = "setId",
            description = "ID of the flashcard set",
            required = true,
            in = PATH
    )
    @Parameter(
            name = "flashcardId",
            description = "ID of the flashcard",
            required = true,
            in = PATH
    )
    public ResponseEntity<ApiResponse<FlashcardStudySessionStartResponseDto>> startStudySession(
            @PathVariable Long setId,
            @PathVariable Long flashcardId
    ) throws BadRequestException {

        FlashcardStudySessionStartResponseDto flashcardStudySessionStartResponseDto = flashcardStudySessionService.startOrResumeSession(setId, flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionStartResponseDto, null));
    }

    @PutMapping("/{sessionId}/progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Sync flashcard study session progress",
            description = "Update the current progress of the study session"
    )
    @Parameter(
            name = "sessionId",
            description = "ID of the study session",
            required = true,
            in = PATH
    )
    public ResponseEntity<ApiResponse<Object>> syncStudySessionProgress(
            @PathVariable Long sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Study session progress data", required = true)
            @Valid @RequestBody FlashcardStudySessionSyncRequestDto flashcardStudySessionSyncRequestDto
    ) throws BadRequestException {

        FlashcardStudySessionStatusResponseDto responseDto = flashcardStudySessionService.syncSessionProgress(sessionId, flashcardStudySessionSyncRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Session progress synced successfully", responseDto, null));
    }

    @GetMapping("/{sessionId}/result")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get flashcard study session result",
            description = "Return the final result of the study session"
    )
    @Parameter(
            name = "sessionId",
            description = "ID of the study session",
            required = true,
            in = PATH
    )
    public ResponseEntity<ApiResponse<FlashcardStudySessionResultResponseDto>> getStudySessionResult(
            @PathVariable Long sessionId
    ) throws BadRequestException {

        FlashcardStudySessionResultResponseDto flashcardStudySessionResultResponseDto = flashcardStudySessionService.getSessionResult(sessionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", flashcardStudySessionResultResponseDto, null));
    }

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cancel flashcard study session",
            description = "Cancel an ongoing study session (user doesn't want to continue)"
    )
    @Parameter(
            name = "sessionId",
            description = "ID of the study session to cancel",
            required = true,
            in = PATH
    )
    public ResponseEntity<ApiResponse<Object>> cancelStudySession(
            @PathVariable Long sessionId
    ) throws BadRequestException {

        flashcardStudySessionService.cancelSession(sessionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Study session cancelled successfully", null, null));
    }
}
