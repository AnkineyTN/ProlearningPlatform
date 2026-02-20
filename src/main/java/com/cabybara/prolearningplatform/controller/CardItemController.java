package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemsDeletionRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
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

@RestController
@RequestMapping("/sets/{setId}/flashcards/{flashcardId}/cards")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard")
public class CardItemController {
    private final CardItemService cardItemService;

    @Operation(
            summary = "Add a/many cards to existed flashcard",
            description = "This api used to add a or many card items to existed flashcard"
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public ResponseEntity<ApiResponse<DetailFlashcardResponseDto>> addCardToFlashcard(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId,

            @Valid @RequestBody List<CardItemCreateRequestDto> cardItemCreateRequestDtos
    ) {
        DetailFlashcardResponseDto detailFlashcardResponseDto = cardItemService.addCardToFlashcard(setId, flashcardId, cardItemCreateRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Add card successfully", detailFlashcardResponseDto, null));
    }

    @Operation(
            summary = "Update a specific card in flashcard",
            description = "Update a existing card item of the flashcard in a set"
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CardItemResponseDto>> updateCardItems(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to update", required = true)
            @PathVariable Long flashcardId,

            @Parameter(description = "The ID of the Card to update", required = true)
            @PathVariable Long cardId,

            @Valid @RequestBody CardItemUpdatingRequestDto cardItemUpdatingRequestDto
    ) {
        CardItemResponseDto updatedCardItem = cardItemService.updateCardItem(setId, flashcardId, cardId, cardItemUpdatingRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update card successfully", updatedCardItem, null));
    }

    @Operation(
            summary = "Update multiple Card Items from a specific Flashcard",
            description = "Deletes a list of Card Items based on the provided IDs, all belonging to a specific Flashcard within a Set."
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("")
    public ResponseEntity<ApiResponse<List<CardItemResponseDto>>> updateCardItems(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to update", required = true)
            @PathVariable Long flashcardId,

            @Valid  @RequestBody List<CardItemUpdatingRequestDto> cardItemUpdatingRequestDto
    ) {
        List<CardItemResponseDto> updatedCardItems = cardItemService.updateCardItems(setId, flashcardId, cardItemUpdatingRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update cards successfully", updatedCardItems, null));
    }

    @Operation(
            summary = "Delete a specific card in flashcard",
            description = "Delete a existing card item using its id"
    )
    @PreAuthorize("isAuthenticated()")
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

    @Operation(
            summary = "Deletes multiple Card Items from a specific Flashcard",
            description = "Deletes a list of Card Items based on the provided IDs, all belonging to a specific Flashcard within a Set. **This operation requires a request body.**"
    )
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<String>> deleteCardItems(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId,

            @Valid @RequestBody CardItemsDeletionRequestDto cardItemsDeletionRequestDto
    ) throws BadRequestException {
        cardItemService.deleteCards(setId, flashcardId, cardItemsDeletionRequestDto.getCardIds());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete cards successfully", null, null));
    }
}
