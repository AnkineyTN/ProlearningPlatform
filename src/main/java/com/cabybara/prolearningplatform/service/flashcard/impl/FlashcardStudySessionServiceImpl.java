package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.FlashcardStudySessionMapper;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.Flashcard;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySessionLogItem;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.FlashcardStudySessionRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardReviewService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardStudySessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class FlashcardStudySessionServiceImpl implements FlashcardStudySessionService {
    private final FlashcardStudySessionRepository flashcardStudySessionRepository;
    private final FlashcardStudySessionMapper flashcardStudySessionMapper;
    private final AuthenticationContext authenticationContext;
    private final CardItemRepository cardItemRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final CardItemService cardItemService;
    private final FlashcardReviewService flashcardReviewService;

    @Override
    public FlashcardStudySessionStatusResponseDto checkStudySessionStatus(Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession inProgressStudySession =
                flashcardStudySessionRepository.findByUserIdAndFlashcardIdAndStatus(userId, flashcardId, FlashcardStudySessionStatus.IN_PROGRESS)
                        .orElse(null);

        if (inProgressStudySession == null) {
            return null;
        }

        return flashcardStudySessionMapper.toFlashcardStudySessionStatusResponse(inProgressStudySession);
    }

    @Override
    public FlashcardStudySessionStartResponseDto startOrResumeSession(Long setId, Long flashcardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        Optional<FlashcardStudySession> existingSession = flashcardStudySessionRepository
                .findByUserIdAndFlashcardIdAndStatus(userId, flashcardId, FlashcardStudySessionStatus.IN_PROGRESS);

        if (existingSession.isPresent()) {
            FlashcardStudySession session = existingSession.get();
            List<Long> remainingIds = session.getRemainingCardIds();

            List<CardItem> cardsToLearn = cardItemRepository.findAllByIdIn(remainingIds);

            return flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(session, cardsToLearn);
        }

        List<CardItem> cardsToLearn = cardItemService.getCardsForReview(setId, flashcardId, 20);

        if (cardsToLearn.isEmpty()) {
            throw new BadRequestException("Nothing card to learn");
        }

        List<Long> cardIds = cardsToLearn.stream().map(CardItem::getId).toList();

        Flashcard flashcardSet = flashcardRepository.getReferenceById(flashcardId);

        FlashcardStudySession newSession = FlashcardStudySession.builder()
                .user(user)
                .flashcard(flashcardSet)
                .status(FlashcardStudySessionStatus.IN_PROGRESS)
                .initialCardIds(cardIds)
                .remainingCardIds(new ArrayList<>(cardIds))
                .reviewLog(new ArrayList<>())
                .correctCount(0)
                .incorrectCount(0)
                .lastInteractionAt(Instant.now())
                .build();

        newSession = flashcardStudySessionRepository.save(newSession);

        return flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(newSession, cardsToLearn);
    }

    @Override
    public FlashcardStudySessionResultResponseDto getSessionResult(Long sessionId) throws BadRequestException {
        FlashcardStudySession session = flashcardStudySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != FlashcardStudySessionStatus.COMPLETED) {
            throw new BadRequestException("Session has not completed");
        }

        return flashcardStudySessionMapper.toFlashcardStudySessionResultResponse(session);
    }

    @Override
    @Transactional
    public void syncSessionProgress(Long sessionId, FlashcardStudySessionSyncRequestDto request) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession session = flashcardStudySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Invalid session owner");
        }

        if (session.getStatus() != FlashcardStudySessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Session has ended");
        }

        List<Long> remainingIds = session.getRemainingCardIds();
        List<FlashcardStudySessionLogItem> logs = session.getReviewLog();

        List<Long> cardIdsToUpdate = request.getCardItemReviews().stream()
                .map(CardItemReviewRequestDto::getCardId)
                .toList();

        List<CardItem> cards = cardItemRepository.findAllById(cardIdsToUpdate);

        Map<Long, CardItem> cardMap = cards.stream()
                .collect(Collectors.toMap(CardItem::getId, Function.identity()));

        for (CardItemReviewRequestDto reviewItem : request.getCardItemReviews()) {
            CardItem card = cardMap.get(reviewItem.getCardId());

            if (card != null) {
                flashcardReviewService.calculateSpacedRepetition(card, reviewItem.isKnown());

                FlashcardStudySessionLogItem logItem = FlashcardStudySessionLogItem.builder()
                        .cardId(reviewItem.getCardId())
                        .isKnown(reviewItem.isKnown())
                        .reviewedAt(OffsetDateTime.now())
                        .build();
                logs.add(logItem);

                remainingIds.remove(reviewItem.getCardId());

                if (reviewItem.isKnown()) {
                    session.setCorrectCount(session.getCorrectCount() + 1);
                } else {
                    session.setIncorrectCount(session.getIncorrectCount() + 1);
                }
            }
        }

        cardItemRepository.saveAll(cards);

        session.setLastInteractionAt(Instant.now());
        if (remainingIds.isEmpty()) {
            session.setStatus(FlashcardStudySessionStatus.COMPLETED);
        }

        session.setRemainingCardIds(remainingIds);
        session.setReviewLog(logs);

        flashcardStudySessionRepository.save(session);
    }
}
