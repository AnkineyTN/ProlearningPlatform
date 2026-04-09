package com.cabybara.prolearningplatform.service.review.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleCardDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.ReviewBundleRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.review.ReviewBundleService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewBundleServiceImpl implements ReviewBundleService {

    private final ReviewBundleRepository reviewBundleRepository;
    private final CardItemRepository cardItemRepository;
    private final AIFlashcardService aiFlashcardService;
    private final AIExamService aiExamService;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public ReviewBundle createBundle(Long userId, List<Long> cardIds,
                                     OffsetDateTime periodFrom,
                                     OffsetDateTime periodTo,
                                     OffsetDateTime expiresAt) {
        ReviewBundle bundle = new ReviewBundle();
        bundle.setUserId(userId);
        bundle.setCardIds(cardIds);
        bundle.setPeriodFrom(periodFrom);
        bundle.setPeriodTo(periodTo);
        bundle.setExpiresAt(expiresAt);
        return reviewBundleRepository.save(bundle);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewBundleResponseDto getBundle(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);

        List<CardItem> cards = cardItemRepository.findAllById(bundle.getCardIds());

        List<ReviewBundleCardDto> cardDtos = cards.stream()
                .map(c -> new ReviewBundleCardDto(c.getId(), c.getFrontCard(), c.getBackCard()))
                .toList();

        return new ReviewBundleResponseDto(
                bundle.getId(),
                bundle.getPeriodFrom(),
                bundle.getPeriodTo(),
                bundle.getExpiresAt(),
                cardDtos.size(),
                cardDtos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GenerateFlashcardByAIResponseDto generateFlashcard(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardContent> cards = loadCardContents(bundle);
        return aiFlashcardService.generateFlashcardFromReview(cards);
    }

    @Override
    @Transactional(readOnly = true)
    public GenerateExamByAIResponseDto generateExam(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardContent> cards = loadCardContents(bundle);
        return aiExamService.generateExamFromReview(cards);
    }

    private ReviewBundle findBundleForCurrentUser(Long bundleId) {
        Long userId = authenticationContext.getCurrentUserId();
        return reviewBundleRepository.findByIdAndUserId(bundleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review bundle not found with id: " + bundleId));
    }

    private List<CardContent> loadCardContents(ReviewBundle bundle) {
        return cardItemRepository.findAllById(bundle.getCardIds())
                .stream()
                .map(c -> new CardContent(c.getFrontCard(), c.getBackCard()))
                .toList();
    }
}
