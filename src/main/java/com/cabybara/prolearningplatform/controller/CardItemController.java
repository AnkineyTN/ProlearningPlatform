package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.CardItemDeletionRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/cards")
@RequiredArgsConstructor
@Tag(name = "Flashcard")
public class CardItemController {
    private final CardItemService cardItemService;

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
        DetailFlashcardResponseDto detailFlashcardResponseDto = cardItemService.addCardToFlashcard(setId, flashcardId, cardItemCreateRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Add card successfully", detailFlashcardResponseDto, null));
    }

    @PatchMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CardItemResponseDto>> updateCardItems(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to update", required = true)
            @PathVariable Long flashcardId,

            @Parameter(description = "The ID of the Card to update", required = true)
            @PathVariable Long cardId,

            @RequestBody CardItemUpdatingRequestDto cardItemUpdatingRequestDto
    ) {
        CardItemResponseDto updatedCardItem = cardItemService.updateCardItem(setId, flashcardId, cardId, cardItemUpdatingRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update card successfully", updatedCardItem, null));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<String>> deleteCardItem(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId,

            @Parameter(description = "The ID of the Card", required = true)
            @PathVariable Long cardId
    ) throws BadRequestException {
        cardItemService.deleteCard(setId, flashcardId, cardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete card successfully", null, null));
    }

    @DeleteMapping("")
    public ResponseEntity<ApiResponse<String>> deleteCardItems(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId,

            @RequestBody CardItemDeletionRequestDto cardItemDeletionRequestDto
    ) throws BadRequestException {
        cardItemService.deleteCards(setId, flashcardId, cardItemDeletionRequestDto.getCardIds());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete cards successfully", null, null));
    }
}
