package com.cabybara.prolearningplatform.service.review.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleCardDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleListItemDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.exception.BadRequestException;
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
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public ReviewBundle createBundle(Long userId, Long setId, List<Long> cardIds,
                                     OffsetDateTime periodFrom,
                                     OffsetDateTime periodTo) {
        ReviewBundle bundle = new ReviewBundle();
        bundle.setUserId(userId);
        bundle.setSetId(setId);
        bundle.setCardIds(cardIds);
        bundle.setPeriodFrom(periodFrom);
        bundle.setPeriodTo(periodTo);
        return reviewBundleRepository.save(bundle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewBundleListItemDto> getBundles() {
        Long userId = authenticationContext.getCurrentUserId();
        return reviewBundleRepository.findAllByUserIdOrderByPeriodToDesc(userId)
                .stream()
                .map(b -> new ReviewBundleListItemDto(
                        b.getId(),
                        b.getSetId(),
                        b.getPeriodFrom(),
                        b.getPeriodTo(),
                        b.getCardIds().size()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void dismissBundle(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        reviewBundleRepository.delete(bundle);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewBundleResponseDto getBundle(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);

        List<ReviewBundleCardDto> cardDtos = cardItemRepository.findCardDtosByIds(bundle.getCardIds())
                .stream()
                .map(p -> new ReviewBundleCardDto(p.getId(), p.getFrontCard(), p.getBackCard()))
                .toList();

        return new ReviewBundleResponseDto(
                bundle.getId(),
                bundle.getPeriodFrom(),
                bundle.getPeriodTo(),
                cardDtos.size(),
                cardDtos
        );
    }

    @Override
    @Transactional
    public FlashcardResponseDto generateFlashcard(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardItem> cards = cardItemRepository.findAllById(bundle.getCardIds());

        if (cards.isEmpty()) {
            throw new BadRequestException("Review bundle has no cards to generate flashcard from");
        }

        String title = String.format("Ôn tập sai: %s – %s",
                bundle.getPeriodFrom().format(PERIOD_FORMATTER),
                bundle.getPeriodTo().format(PERIOD_FORMATTER_YEAR));
        String description = String.format("Tổng hợp %d thẻ trả lời sai trong tuần", cards.size());

        return flashcardService.addFlashcardFromReview(cards, title, description, bundle.getSetId());
    }

    @Override
    @Transactional
    public ExamResponseDto generateExam(Long bundleId) {
        ReviewBundle bundle = findBundleForCurrentUser(bundleId);
        List<CardContent> cards = loadCardContents(bundle);

        if (cards.isEmpty()) {
            throw new BadRequestException("Review bundle has no cards to generate exam from");
        }

        CreateExamFromReviewRequestDto dto = aiExamService.generateExamFromCards(cards);
        return examService.createExamFromReview(dto, bundle.getSetId());
    }

    private ReviewBundle findBundleForCurrentUser(Long bundleId) {
        Long userId = authenticationContext.getCurrentUserId();
        return reviewBundleRepository.findByIdAndUserId(bundleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review bundle not found with id: " + bundleId));
    }

    private List<CardContent> loadCardContents(ReviewBundle bundle) {
        return cardItemRepository.findCardContentsByIds(bundle.getCardIds())
                .stream()
                .map(p -> new CardContent(p.getFront(), p.getBack()))
                .toList();
    }
}
