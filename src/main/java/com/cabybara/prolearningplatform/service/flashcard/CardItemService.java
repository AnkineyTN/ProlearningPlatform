package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.model.CardItem;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface CardItemService {
    DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos);

    CardItemResponseDto updateCardItem(Long setId, Long flashcardId, Long cardId, CardItemUpdatingRequestDto updateFlashcardRequestDto);

    List<CardItemResponseDto> updateCardItems(Long setId, Long flashcardId, List<CardItemUpdatingRequestDto> updatingRequestDtos);

    void deleteCards(Long setId, Long flashcardId, List<Long> cardIds) throws BadRequestException;

    void deleteCard(Long setId, Long flashcardId, Long cardId) throws BadRequestException;

    List<CardItem> getCardsForReview(Long setId, Long flashcardSetId, int limit);

    List<CardItem> getAllCardsForReview(Long setId, Long flashcardId, int limit);
}
