package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardLearnResponseDto;
import com.cabybara.prolearningplatform.model.CardItem;

import java.util.List;

public interface FlashcardReviewService {
    List<CardLearnResponseDto> getCardsForReview(Long setId, Long flashcardSetId, int limit);

    void calculateSpacedRepetition(CardItem card, boolean isKnown);

    void processBatchReview(Long setId, Long flashcardId, FlashcardStudySessionSyncRequestDto requestDto);
}