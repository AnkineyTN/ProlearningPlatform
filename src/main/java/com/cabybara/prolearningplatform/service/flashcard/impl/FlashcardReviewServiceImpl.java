package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardLearnResponseDto;
import com.cabybara.prolearningplatform.enums.CardStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardReviewService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardReviewServiceImpl implements FlashcardReviewService {
    private final FlashcardRepository flashcardRepository;
    private final AuthenticationContext authenticationContext;
    private final CardItemService cardItemService;
    private CardItemRepository cardItemRepository;
    private CardItemMapper cardItemMapper;

    @Override
    public List<CardLearnResponseDto> getCardsForReview(Long setId, Long flashcardId, int limit) {
        List<CardItem> finalQueue = cardItemService.getCardsForReview(setId, flashcardId, limit);

        return finalQueue.stream().map(cardItemMapper::toCardLearnResponseDto).toList();
    }

    @Override
    public void processBatchReview(Long setId, Long flashcardId, FlashcardStudySessionSyncRequestDto requestDto) {
        Long userId = authenticationContext.getCurrentUserId();
        if (!flashcardRepository.existsBySetIdAndIdAndUserId(setId, flashcardId, userId)) {
            throw new ResourceNotFoundException("Flashcard not found or invalid");
        }

        List<Long> cardIds = requestDto.getCardItemReviews().stream()
                .map(CardItemReviewRequestDto::getCardId)
                .toList();

        List<CardItem> cards = cardItemRepository.findAllById(cardIds);

        Map<Long, Boolean> reviewMap = requestDto.getCardItemReviews().stream()
                .collect(Collectors.toMap(CardItemReviewRequestDto::getCardId, CardItemReviewRequestDto::isKnown));

        for (CardItem card : cards) {
            boolean isKnown = reviewMap.get(card.getId());
            calculateSpacedRepetition(card, isKnown);
        }

        cardItemRepository.saveAll(cards);

        // trigger updatedAt on parent flashcard
    }

    /**
     * SM-2 (SuperMemo 2).
     * Piotr Wozniak, adapt for binary input:
     * - isKnown = true  → quality = 4 (correct after hesitation) | 5 (Prefect - instant answer)
     * - isKnown = false → quality = 1 (incorrect, barely remembered)
     * EF (Ease Factor):
     *   EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
     *   EF' = max(1.3, EF')
     * Interval:
     *   Correct (q >= 3): 1d → 6d → interval * EF
     *   Incorrect (q < 3): reset ve 1d, repetitions = 0
     */
    @Override
    public void calculateSpacedRepetition(CardItem card, boolean isKnown) {
        int quality = isKnown ? 5 : 1;

        float ef = card.getEaseFactor();
        ef = ef + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f));
        ef = Math.max(1.3f, ef);
        card.setEaseFactor(ef);

        if (quality >= 3) {
            int reps = card.getRepetitions();
            if (reps == 0) {
                card.setIntervalDays(1);
            } else if (reps == 1) {
                card.setIntervalDays(6);
            } else {
                card.setIntervalDays(Math.round(card.getIntervalDays() * ef));
            }
            card.setRepetitions(reps + 1);
            card.setCardStatus(CardStatus.KNOWN);
        } else {
            card.setRepetitions(0);
            card.setIntervalDays(1);
            card.setCardStatus(CardStatus.UNKNOWN);
        }

        card.setNextReviewAt(OffsetDateTime.now().plusDays(card.getIntervalDays()));
    }
}
