package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/cards")
@RequiredArgsConstructor
@Tag(name = "Flashcard")
public class CardItemController {

    private final FlashcardService flashcardService;

    @Operation(
            summary = "Add a/many cards to existed flashcard",
            description = "This api used to add a or many card items to existed flashcard"
    )
    @PostMapping("")
    public ResponseEntity<ApiResponse<DetailFlashcardResponseDto>> addCardToFlashcard(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId,

            @RequestBody List<CardItemCreateRequestDto> cardItemCreateRequestDtos
    ) {
        DetailFlashcardResponseDto detailFlashcardResponseDto = flashcardService.addCardToFlashcard(setId, flashcardId, cardItemCreateRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Add card successfully", detailFlashcardResponseDto, null));
    }
}
