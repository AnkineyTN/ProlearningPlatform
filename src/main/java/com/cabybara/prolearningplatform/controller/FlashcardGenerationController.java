package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteToSetRequestDto;
import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.ResponseError;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.permission.annotation.AiRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/flashcards")
@RequiredArgsConstructor
public class FlashcardGenerationController {
    private static final String ERROR_MESSAGE = "Generate flashcard by notes successfully";

    private final FlashcardService flashcardService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/ai-note")
    @AiRateLimit(type = "AI_GENERATION")
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByNoteForTargetSet(
            @Valid @RequestBody GenerateFlashcardByNoteToSetRequestDto request
    ) {
        GenerateFlashcardByAIResponseDto response = flashcardService.generateFlashcardByNotesForTargetSet(request);
        return new ResponseData<>(HttpStatus.OK.value(), ERROR_MESSAGE, response);
    }
}
