package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/cards")
@RequiredArgsConstructor
public class CardItemController {

    private final FlashcardService flashcardService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> addCardToFlashcard(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @RequestBody List<CardItemCreateRequestDto> cardItemCreateRequestDtos
    ) {
        DetailFlashcardResponseDto detailFlashcardResponseDto = flashcardService.addCardToFlashcard(setId, flashcardId, cardItemCreateRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Add card successfully", detailFlashcardResponseDto, null));
    }
}
