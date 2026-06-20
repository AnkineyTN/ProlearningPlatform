package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.enums.CardStatus;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.flashcard.impl.FlashcardReviewServiceImpl;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FlashcardReviewServiceImplTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private CardItemService cardItemService;

    @Mock
    private CardItemRepository cardItemRepository;

    @Mock
    private CardItemMapper cardItemMapper;

    private FlashcardReviewServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new FlashcardReviewServiceImpl(flashcardRepository, authenticationContext, cardItemService);

        Field repoField = FlashcardReviewServiceImpl.class.getDeclaredField("cardItemRepository");
        repoField.setAccessible(true);
        repoField.set(service, cardItemRepository);

        Field mapperField = FlashcardReviewServiceImpl.class.getDeclaredField("cardItemMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, cardItemMapper);
    }

    @Test
    void calculateSpacedRepetitionWhenKnown() {
        CardItem card = new CardItem();
        card.setId(1L);
        card.setIntervalDays(1);
        card.setEaseFactor(2.5f);
        card.setRepetitions(0);
        card.setCardStatus(CardStatus.NEW);

        service.calculateSpacedRepetition(card, true);

        assertEquals(1, card.getIntervalDays());
        assertEquals(1, card.getRepetitions());
        assertEquals(2.6f, card.getEaseFactor(), 0.001f);
        assertEquals(CardStatus.KNOWN, card.getCardStatus());
        assertNotNull(card.getNextReviewAt());
    }

    @Test
    void calculateSpacedRepetitionWhenUnknown() {
        CardItem card = new CardItem();
        card.setId(1L);
        card.setIntervalDays(5);
        card.setEaseFactor(2.5f);
        card.setRepetitions(3);
        card.setCardStatus(CardStatus.KNOWN);
        card.setNextReviewAt(OffsetDateTime.now());

        service.calculateSpacedRepetition(card, false);

        assertEquals(1, card.getIntervalDays());
        assertEquals(0, card.getRepetitions());
        assertEquals(1.96f, card.getEaseFactor(), 0.001f);
        assertEquals(CardStatus.UNKNOWN, card.getCardStatus());
    }

    @Test
    void calculateSpacedRepetitionSecondCorrect() {
        CardItem card = new CardItem();
        card.setId(1L);
        card.setIntervalDays(1);
        card.setEaseFactor(2.5f);
        card.setRepetitions(1);
        card.setCardStatus(CardStatus.KNOWN);
        card.setNextReviewAt(OffsetDateTime.now());

        service.calculateSpacedRepetition(card, true);

        assertEquals(6, card.getIntervalDays());
        assertEquals(2, card.getRepetitions());
        assertEquals(2.6f, card.getEaseFactor(), 0.001f);
        assertEquals(CardStatus.KNOWN, card.getCardStatus());
    }

    @Test
    void calculateSpacedRepetitionThirdCorrect() {
        CardItem card = new CardItem();
        card.setId(1L);
        card.setIntervalDays(6);
        card.setEaseFactor(2.5f);
        card.setRepetitions(2);
        card.setCardStatus(CardStatus.KNOWN);
        card.setNextReviewAt(OffsetDateTime.now());

        service.calculateSpacedRepetition(card, true);

        assertEquals(16, card.getIntervalDays());
        assertEquals(3, card.getRepetitions());
        assertEquals(2.6f, card.getEaseFactor(), 0.001f);
        assertEquals(CardStatus.KNOWN, card.getCardStatus());
    }

    @Test
    void calculateSpacedRepetitionEaseFactorMinFloor() {
        CardItem card = new CardItem();
        card.setId(1L);
        card.setIntervalDays(5);
        card.setEaseFactor(1.5f);
        card.setRepetitions(2);
        card.setCardStatus(CardStatus.KNOWN);
        card.setNextReviewAt(OffsetDateTime.now());

        service.calculateSpacedRepetition(card, false);

        assertEquals(1.3f, card.getEaseFactor(), 0.001f);
        assertEquals(1, card.getIntervalDays());
        assertEquals(0, card.getRepetitions());
    }
}
