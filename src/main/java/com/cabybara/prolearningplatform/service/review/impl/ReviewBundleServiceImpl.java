package com.cabybara.prolearningplatform.service.review.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleCardDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.ReviewBundleRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.ExamService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.review.ReviewBundleService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewBundleServiceImpl implements ReviewBundleService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter PERIOD_FORMATTER_YEAR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReviewBundleRepository reviewBundleRepository;
    private final CardItemRepository cardItemRepository;
    private final AIExamService aiExamService;
    private final FlashcardService flashcardService;
    private final ExamService examService;
    private final ObjectMapper objectMapper;
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
    @Transactional
    public FlashcardResponseDto generateFlashcard(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardItem> cards = cardItemRepository.findAllById(bundle.getCardIds());

        String title = String.format("Ôn tập sai: %s – %s",
                bundle.getPeriodFrom().format(PERIOD_FORMATTER),
                bundle.getPeriodTo().format(PERIOD_FORMATTER_YEAR));
        String description = String.format("Tổng hợp %d thẻ trả lời sai trong tuần", cards.size());

        return flashcardService.addFlashcardFromReview(cards, title, description);
    }

    @Override
    @Transactional
    public ExamResponseDto generateExam(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardContent> cards = loadCardContents(bundle);

        String aiContent = aiExamService.generateExamFromReview(cards).getContent();

        if (aiContent == null || aiContent.isBlank()) {
            throw new RuntimeException("AI service returned empty response for exam generation");
        }

        CreateExamFromReviewRequestDto dto = parseJson(aiContent, CreateExamFromReviewRequestDto.class);
        return examService.createExamFromReview(dto);
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

    private <T> T parseJson(String json, Class<T> targetType) {
        try {
            return objectMapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response as " + targetType.getSimpleName(), e);
        }
    }
}
