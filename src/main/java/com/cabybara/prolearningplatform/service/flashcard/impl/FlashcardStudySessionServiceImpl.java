package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.enums.StudyMode;
import com.cabybara.prolearningplatform.exception.FlashcardStudySessionException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.FlashcardStudySessionMapper;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySessionLogItem;
import com.cabybara.prolearningplatform.model.flashcard_study_session.StudySessionReviewLog;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardReviewService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardStudySessionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final SetRepository setRepository;

    @Override
    public List<FlashcardStudySessionStatusResponseDto> checkStudySessionStatus(Long setId, Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();

        List<FlashcardStudySession> inProgressStudySessions =
                flashcardStudySessionRepository.findByUserIdAndSetIdAndFlashcardIdAndStatus(userId, setId, flashcardId, FlashcardStudySessionStatus.IN_PROGRESS);

        if (inProgressStudySessions.isEmpty()) {
            return Collections.emptyList();
        }

        return inProgressStudySessions.stream().map(flashcardStudySessionMapper::toFlashcardStudySessionStatusResponse).toList();
    }

    @Override
    public FlashcardStudySessionStartResponseDto startOrResumeSession(Long setId, Long flashcardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        Optional<FlashcardStudySession> existingInProgressSession = flashcardStudySessionRepository
                .findByUserIdAndFlashcardIdAndStatus(userId, flashcardId, FlashcardStudySessionStatus.IN_PROGRESS);

        if (existingInProgressSession.isPresent()) {
            FlashcardStudySession session = existingInProgressSession.get();
            List<Long> remainingIds = session.getRemainingCardIds();

            List<CardItem> cardsToLearn = cardItemRepository.findAllByIdIn(remainingIds);

            String message = session.getStudyMode() == StudyMode.SPACED_REPETITION
                ? "Resuming spaced repetition session"
                : "Resuming review session";

            return flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(session, cardsToLearn, message);
        }

        // Check for recently completed session with incorrect cards (Continue Learning)
        Optional<FlashcardStudySession> lastCompleted = flashcardStudySessionRepository
                .findLatestSessionByStatus(userId, flashcardId, FlashcardStudySessionStatus.COMPLETED);

        if (lastCompleted.isPresent() && lastCompleted.get().getIncorrectCount() != null
                && lastCompleted.get().getIncorrectCount() > 0) {
            FlashcardStudySession prevSession = lastCompleted.get();

            List<Long> incorrectCardIds = flashcardStudySessionRepository
                    .findIncorrectCardIdsBySessionId(prevSession.getId());

            if (!incorrectCardIds.isEmpty()) {
                List<CardItem> cardsToLearn = cardItemRepository.findAllByIdIn(incorrectCardIds);

                if (!cardsToLearn.isEmpty()) {
                    Set set = setRepository.getReferenceById(setId);
                    Flashcard flashcardSet = flashcardRepository.getReferenceById(flashcardId);

                    FlashcardStudySession newSession = FlashcardStudySession.builder()
                            .user(user)
                            .set(set)
                            .flashcard(flashcardSet)
                            .status(FlashcardStudySessionStatus.IN_PROGRESS)
                            .studyMode(StudyMode.REVIEW)
                            .initialCardIds(incorrectCardIds)
                            .remainingCardIds(new ArrayList<>(incorrectCardIds))
                            .reviewLogs(new ArrayList<>())
                            .correctCount(0)
                            .incorrectCount(0)
                            .lastInteractionAt(Instant.now())
                            .build();

                    newSession = flashcardStudySessionRepository.save(newSession);

                    return flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(
                            newSession, cardsToLearn,
                            "Continuing from previous session with " + incorrectCardIds.size() + " incorrect cards");
                }
            }
        }

        // Try to get cards based on spaced repetition first
        List<CardItem> cardsToLearn = cardItemService.getCardsForReview(setId, flashcardId, 20);
        StudyMode studyMode = StudyMode.SPACED_REPETITION;
        String message = "Starting spaced repetition session";

        // If no SR cards available, fallback to review mode with all cards
        if (cardsToLearn.isEmpty()) {
            cardsToLearn = cardItemService.getAllCardsForReview(setId, flashcardId, 20);
            studyMode = StudyMode.REVIEW;
            message = "No cards due for review. Starting review mode with all cards.";

            if (cardsToLearn.isEmpty()) {
                throw new FlashcardStudySessionException("This flashcard has no cards to study. Please add some cards first.");
            }
        }

        List<Long> cardIds = cardsToLearn.stream().map(CardItem::getId).toList();

        Set set = setRepository.getReferenceById(setId);
        Flashcard flashcardSet = flashcardRepository.getReferenceById(flashcardId);

        FlashcardStudySession newSession = FlashcardStudySession.builder()
                .user(user)
                .set(set)
                .flashcard(flashcardSet)
                .status(FlashcardStudySessionStatus.IN_PROGRESS)
                .studyMode(studyMode)
                .initialCardIds(cardIds)
                .remainingCardIds(new ArrayList<>(cardIds))
                .reviewLogs(new ArrayList<>())
                .correctCount(0)
                .incorrectCount(0)
                .lastInteractionAt(Instant.now())
                .build();

        newSession = flashcardStudySessionRepository.save(newSession);

        return flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(newSession, cardsToLearn, message);
    }

    @Override
    public FlashcardStudySessionResultResponseDto getSessionResult(Long sessionId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession session = flashcardStudySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: session does not belong to current user");
        }

        if (session.getStatus() != FlashcardStudySessionStatus.COMPLETED) {
            throw new FlashcardStudySessionException("Session has not completed");
        }

        return flashcardStudySessionMapper.toFlashcardStudySessionResultResponse(session);
    }

    @Override
    @Transactional
    public FlashcardStudySessionStatusResponseDto syncSessionProgress(Long sessionId, FlashcardStudySessionSyncRequestDto request) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession session = flashcardStudySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Invalid session owner");
        }

        if (session.getStatus() != FlashcardStudySessionStatus.IN_PROGRESS) {
            throw new FlashcardStudySessionException("Session has ended");
        }

        List<Long> remainingIds = session.getRemainingCardIds();
        List<StudySessionReviewLog> logs = session.getReviewLogs();

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

                StudySessionReviewLog reviewLog = StudySessionReviewLog.builder()
                        .card(card)
                        .known(reviewItem.isKnown())
                        .reviewedAt(OffsetDateTime.now())
                        .build();
                session.addReviewLog(reviewLog);

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
        session.setReviewLogs(logs);

        flashcardStudySessionRepository.save(session);

        return flashcardStudySessionMapper.toFlashcardStudySessionStatusResponse(session);
    }

    @Override
    public void cancelSession(Long sessionId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession session = flashcardStudySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Invalid session owner");
        }

        if (session.getStatus() != FlashcardStudySessionStatus.IN_PROGRESS) {
            throw new FlashcardStudySessionException("Session is not in progress");
        }

        session.setStatus(FlashcardStudySessionStatus.CANCELLED);
        session.setLastInteractionAt(Instant.now());

        flashcardStudySessionRepository.save(session);
    }
}
