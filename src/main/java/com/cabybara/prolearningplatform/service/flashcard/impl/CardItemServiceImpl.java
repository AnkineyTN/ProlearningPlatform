package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.Flashcard;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardItemServiceImpl implements CardItemService {
    private final FlashcardService flashcardService;
    private final CardItemMapper cardItemMapper;
    private final AuthenticationContext authenticationContext;
    private final CardItemRepository cardItemRepository;

    @Override
    @Transactional
    public DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos) {
        Flashcard flashcard = flashcardService.getFlashcardById(flashcardId);

        List<CardItem> cardItems = dtos.stream()
                .map(dto -> {
                    CardItem card = cardItemMapper.toCardItem(dto);

                    card.setFlashcard(flashcard);

                    return card;
                })
                .toList();

        flashcard.getCards().addAll(cardItems);

        return flashcardService.updateFlashcard(flashcard);
    }

    @Override
    public CardItemResponseDto updateCardItem(Long setId, Long flashcardId, Long cardId, CardItemUpdatingRequestDto updateFlashcardRequestDto) {
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card Item with id: " + cardId + " not found."));

        cardItemMapper.updateCardFromDto(updateFlashcardRequestDto, cardItem);

        return cardItemMapper.toCardItemResponseDto(cardItemRepository.save(cardItem));
    }

    @Override
    @Transactional
    public void deleteCard(Long setId, Long flashcardId, Long cardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found."));

        if (!cardItem.getFlashcard().getId().equals(flashcardId) ||
                !cardItem.getFlashcard().getSet().getId().equals(setId) ||
                !cardItem.getFlashcard().getSet().getUser().getId().equals(userId)) {

            throw new BadRequestException("The card does not match the provided ownership criteria.");
        }

        cardItemRepository.delete(cardItem);
    }

    @Override
    @Transactional
    public void deleteCards(Long setId, Long flashcardId, List<Long> cardIds) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        int deletedCount = cardItemRepository.deleteAllByIdInAndOwnershipChecks(
                cardIds,
                flashcardId,
                setId,
                userId
        );

        if (deletedCount == 0 && !cardIds.isEmpty()) {
            throw new BadRequestException("Flashcard not found or access denied.");
        }
    }
}
