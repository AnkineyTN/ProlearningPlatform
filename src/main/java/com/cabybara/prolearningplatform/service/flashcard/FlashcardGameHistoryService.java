package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardGameResultRequest;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameHistoryResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameRankingEntryDto;

import java.util.List;

public interface FlashcardGameHistoryService {

    FlashcardGameHistoryResponseDto saveResult(Long flashcardId, FlashcardGameResultRequest request);

    List<FlashcardGameHistoryResponseDto> getUserHistory(Long flashcardId);

    List<FlashcardGameRankingEntryDto> getRanking(Long flashcardId);
}
