package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionLogItemResponseDto;
import com.cabybara.prolearningplatform.enums.CardStatus;
import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.enums.StudyMode;
import com.cabybara.prolearningplatform.exception.FlashcardStudySessionException;
import com.cabybara.prolearningplatform.mapper.FlashcardStudySessionMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import com.cabybara.prolearningplatform.model.flashcard_study_session.StudySessionReviewLog;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.FlashcardStudySessionRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.flashcard.impl.FlashcardStudySessionServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardStudySessionServiceImplTest {

    @Mock
    private FlashcardStudySessionRepository flashcardStudySessionRepository;

    @Mock
    private FlashcardStudySessionMapper flashcardStudySessionMapper;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private CardItemRepository cardItemRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardItemService cardItemService;

    @Mock
    private FlashcardReviewService flashcardReviewService;

    @Mock
    private SetRepository setRepository;

    @Mock
    private CacheManager cacheManager;
    private com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService flashcardPermissionService;

    private FlashcardStudySessionServiceImpl service;

    private User user;
    private Set set;
    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        service = new FlashcardStudySessionServiceImpl(
                flashcardStudySessionRepository,
                flashcardStudySessionMapper,
                authenticationContext,
                cardItemRepository,
                flashcardRepository,
                userRepository,
                cardItemService,
                flashcardReviewService,
                setRepository,
                cacheManager,
                flashcardPermissionService
        );

        user = TestFixtures.user(1L);

        set = new Set();
        set.setId(1L);
        set.setUser(user);

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setSet(set);
        flashcard.setUser(user);

        org.mockito.Mockito.lenient().when(flashcardPermissionService.hasAccess(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
        org.mockito.Mockito.lenient().when(flashcardPermissionService.isOwner(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
    }

    @Test
    void startNewSessionWhenNoneInProgress() throws Exception {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(flashcardStudySessionRepository.findByUserIdAndFlashcardIdAndStatus(
                1L, 1L, FlashcardStudySessionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        CardItem card1 = CardItem.builder().flashcard(flashcard).frontCard("front1").backCard("back1").build();
        card1.setId(1L);
        CardItem card2 = CardItem.builder().flashcard(flashcard).frontCard("front2").backCard("back2").build();
        card2.setId(2L);
        CardItem card3 = CardItem.builder().flashcard(flashcard).frontCard("front3").backCard("back3").build();
        card3.setId(3L);
        List<CardItem> cards = List.of(card1, card2, card3);

        when(cardItemService.getCardsForReview(1L, 1L, 20)).thenReturn(cards);
        when(setRepository.getReferenceById(1L)).thenReturn(set);
        when(flashcardRepository.getReferenceById(1L)).thenReturn(flashcard);

        when(flashcardStudySessionRepository.save(any(FlashcardStudySession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FlashcardStudySessionStartResponseDto expectedResponse = FlashcardStudySessionStartResponseDto.builder()
                .id(1L)
                .studyMode(StudyMode.SPACED_REPETITION)
                .message("Starting spaced repetition session")
                .cards(List.of())
                .build();

        when(flashcardStudySessionMapper.toFlashcardStudySessionStartResponse(
                any(FlashcardStudySession.class), eq(cards), eq("Starting spaced repetition session")))
                .thenReturn(expectedResponse);

        FlashcardStudySessionStartResponseDto result = service.startOrResumeSession(1L, 1L);

        assertNotNull(result);
        assertEquals(StudyMode.SPACED_REPETITION, result.getStudyMode());

        ArgumentCaptor<FlashcardStudySession> sessionCaptor = ArgumentCaptor.forClass(FlashcardStudySession.class);
        verify(flashcardStudySessionRepository).save(sessionCaptor.capture());
        FlashcardStudySession savedSession = sessionCaptor.getValue();
        assertEquals(FlashcardStudySessionStatus.IN_PROGRESS, savedSession.getStatus());
        assertEquals(3, savedSession.getInitialCardIds().size());
    }

    @Test
    void cancelSessionMarksCancelled() throws Exception {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        FlashcardStudySession session = buildInProgressSession();
        session.setId(1L);

        when(flashcardStudySessionRepository.findById(1L)).thenReturn(Optional.of(session));

        service.cancelSession(1L);

        assertEquals(FlashcardStudySessionStatus.CANCELLED, session.getStatus());
        assertNotNull(session.getLastInteractionAt());
        verify(flashcardStudySessionRepository).save(session);
    }

    @Test
    void getSessionResultWithCorrectIncorrectCounts() throws Exception {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        FlashcardStudySession session = buildInProgressSession();
        session.setId(1L);
        session.setStatus(FlashcardStudySessionStatus.COMPLETED);
        session.setCorrectCount(2);
        session.setIncorrectCount(1);

        CardItem card1 = CardItem.builder().build();
        card1.setId(1L);
        CardItem card2 = CardItem.builder().build();
        card2.setId(2L);
        CardItem card3 = CardItem.builder().build();
        card3.setId(3L);

        StudySessionReviewLog log1 = createReviewLog(card1, true);
        StudySessionReviewLog log2 = createReviewLog(card2, true);
        StudySessionReviewLog log3 = createReviewLog(card3, false);

        session.setReviewLogs(new ArrayList<>(List.of(log1, log2, log3)));

        when(flashcardStudySessionRepository.findById(1L)).thenReturn(Optional.of(session));

        when(cardItemRepository.findAllByFlashcardId(1L)).thenReturn(List.of(card1, card2, card3));

        FlashcardStudySessionResultResponseDto expectedResult = FlashcardStudySessionResultResponseDto.builder()
                .sessionId(1L)
                .correctCount(2)
                .incorrectCount(1)
                .logs(List.of())
                .build();

        when(flashcardStudySessionMapper.toFlashcardStudySessionResultResponse(session))
                .thenReturn(expectedResult);

        FlashcardStudySessionResultResponseDto result = service.getSessionResult(1L);

        assertEquals(1L, result.getSessionId());
        assertEquals(2, result.getCorrectCount());
        assertEquals(1, result.getIncorrectCount());
    }

    @Test
    void syncSessionProgressMarksCompletedWhenAllReviewed() throws Exception {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        FlashcardStudySession session = buildInProgressSession();
        session.setId(1L);
        session.setRemainingCardIds(new ArrayList<>(List.of(1L)));
        session.setCorrectCount(0);
        session.setIncorrectCount(0);

        when(flashcardStudySessionRepository.findById(1L)).thenReturn(Optional.of(session));

        CardItem card1 = CardItem.builder().flashcard(flashcard).frontCard("f").backCard("b").build();
        card1.setId(1L);

        CardItemReviewRequestDto reviewRequest = new CardItemReviewRequestDto();
        reviewRequest.setCardId(1L);
        reviewRequest.setKnown(true);

        FlashcardStudySessionSyncRequestDto syncRequest = new FlashcardStudySessionSyncRequestDto();
        syncRequest.setCardItemReviews(List.of(reviewRequest));

        when(cardItemRepository.findAllById(List.of(1L))).thenReturn(List.of(card1));
        when(cardItemRepository.findAllByFlashcardId(1L)).thenReturn(List.of(card1));
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(cacheManager.getCache("flashcard_detail")).thenReturn(null);

        FlashcardStudySessionStatusResponseDto expectedStatus = FlashcardStudySessionStatusResponseDto.builder()
                .id(1L)
                .status(FlashcardStudySessionStatus.COMPLETED)
                .build();

        when(flashcardStudySessionMapper.toFlashcardStudySessionStatusResponse(session))
                .thenReturn(expectedStatus);

        FlashcardStudySessionStatusResponseDto result = service.syncSessionProgress(1L, syncRequest);

        assertNotNull(result);
        verify(flashcardReviewService).calculateSpacedRepetition(card1, true);
        verify(cardItemRepository).saveAll(List.of(card1));
        assertEquals(FlashcardStudySessionStatus.COMPLETED, session.getStatus());
        assertNotNull(session.getLastInteractionAt());
        assertEquals(1, session.getCorrectCount());
        assertTrue(session.getRemainingCardIds().isEmpty());
        verify(flashcardStudySessionRepository).save(session);
    }

    private FlashcardStudySession buildInProgressSession() {
        FlashcardStudySession session = new FlashcardStudySession();
        session.setUser(user);
        session.setSet(set);
        session.setFlashcard(flashcard);
        session.setStatus(FlashcardStudySessionStatus.IN_PROGRESS);
        session.setStudyMode(StudyMode.SPACED_REPETITION);
        session.setInitialCardIds(new ArrayList<>(List.of(1L, 2L, 3L)));
        session.setRemainingCardIds(new ArrayList<>(List.of(1L, 2L, 3L)));
        session.setReviewLogs(new ArrayList<>());
        session.setCorrectCount(0);
        session.setIncorrectCount(0);
        return session;
    }

    private StudySessionReviewLog createReviewLog(CardItem card, boolean known) {
        StudySessionReviewLog log = new StudySessionReviewLog();
        log.setCard(card);
        log.setKnown(known);
        log.setReviewedAt(OffsetDateTime.now());
        return log;
    }
}
