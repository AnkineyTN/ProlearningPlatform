package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardLearnResponseDto;
import com.cabybara.prolearningplatform.enums.CardStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    @Override
    public void calculateSpacedRepetition(CardItem card, boolean isKnown) {
        if (isKnown) {
            if (card.getRepetitions() == 0) {
                card.setIntervalDays(1);
            } else if (card.getRepetitions() == 1) {
                card.setIntervalDays(3);
            } else {
                int newInterval = Math.round(card.getIntervalDays() * card.getEaseFactor());
                card.setIntervalDays(newInterval);
            }

            // bonus --> reduce EF
            card.setEaseFactor((float) (card.getEaseFactor() + 0.1));

            card.setRepetitions(card.getRepetitions() + 1);
            card.setCardStatus(CardStatus.KNOWN);

        } else {
            card.setIntervalDays(0);
            card.setRepetitions(0);

            // punish --> increase EF
            double newEase = Math.max(1.3, card.getEaseFactor() - 0.2);
            card.setEaseFactor((float) newEase);

            card.setCardStatus(CardStatus.UNKNOW);
        }

        calculateDueDate(card);
    }

    private void calculateDueDate(CardItem card) {
        if (card.getIntervalDays() == 0) {
            card.setNextReviewAt(OffsetDateTime.now().plusMinutes(10));
        } else {
            card.setNextReviewAt(OffsetDateTime.now().plusDays(card.getIntervalDays()));
        }
    }
}
