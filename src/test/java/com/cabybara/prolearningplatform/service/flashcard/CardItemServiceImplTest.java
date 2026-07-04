package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.flashcard.impl.CardItemServiceImpl;
import com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardItemServiceImplTest {

    @Mock
    private FlashcardService flashcardService;

    @Mock
    private CardItemMapper cardItemMapper;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private CardItemRepository cardItemRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private FlashcardPermissionService flashcardPermissionService;

    @Test
    void addMultipleCardsToFlashcard() {
        CardItemServiceImpl service = new CardItemServiceImpl(flashcardService, cardItemMapper,
                authenticationContext, cardItemRepository, assetService, flashcardRepository, flashcardPermissionService);

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setSet(set);
        flashcard.setUser(user);
        flashcard.setCards(new ArrayList<>());

        CardItemCreateRequestDto dto1 = CardItemCreateRequestDto.builder()
                .frontCard("Q1")
                .backCard("A1")
                .build();
        CardItemCreateRequestDto dto2 = CardItemCreateRequestDto.builder()
                .frontCard("Q2")
                .backCard("A2")
                .build();
        List<CardItemCreateRequestDto> dtos = List.of(dto1, dto2);

        CardItem card1 = new CardItem();
        CardItem card2 = new CardItem();
        DetailFlashcardResponseDto responseDto = new DetailFlashcardResponseDto();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardPermissionService.canEdit(1L, 1L)).thenReturn(true);
        when(flashcardService.getFlashcardById(1L)).thenReturn(flashcard);
        when(assetService.findAndActivateAssets(any(), eq(1L))).thenReturn(Collections.emptyMap());
        when(cardItemMapper.toCardItem(dto1)).thenReturn(card1);
        when(cardItemMapper.toCardItem(dto2)).thenReturn(card2);
        when(flashcardService.updateFlashcard(flashcard)).thenReturn(responseDto);

        DetailFlashcardResponseDto result = service.addCardToFlashcard(1L, 1L, dtos);

        assertSame(responseDto, result);
        assertEquals(2, flashcard.getCards().size());
        verify(flashcardService).updateFlashcard(flashcard);
    }

    @Test
    void updateCardItemFrontAndBack() {
        CardItemServiceImpl service = new CardItemServiceImpl(flashcardService, cardItemMapper,
                authenticationContext, cardItemRepository, assetService, flashcardRepository, flashcardPermissionService);

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setSet(set);
        flashcard.setUser(user);

        CardItem cardItem = new CardItem();
        cardItem.setId(1L);
        cardItem.setFrontCard("OldFront");
        cardItem.setBackCard("OldBack");
        cardItem.setFlashcard(flashcard);

        CardItemUpdatingRequestDto dto = CardItemUpdatingRequestDto.builder()
                .id(1L)
                .frontCard("NewFront")
                .backCard("NewBack")
                .build();

        CardItemResponseDto responseDto = CardItemResponseDto.builder()
                .id(1L)
                .frontCard("NewFront")
                .backCard("NewBack")
                .build();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(cardItemRepository.findById(1L)).thenReturn(Optional.of(cardItem));
        doAnswer(invocation -> {
            CardItem ci = invocation.getArgument(1);
            ci.setFrontCard("NewFront");
            ci.setBackCard("NewBack");
            return null;
        }).when(cardItemMapper).updateCardFromDto(eq(dto), eq(cardItem));
        when(cardItemRepository.save(cardItem)).thenReturn(cardItem);
        when(cardItemMapper.toCardItemResponseDto(cardItem)).thenReturn(responseDto);

        CardItemResponseDto result = service.updateCardItem(1L, 1L, 1L, dto);

        assertEquals("NewFront", cardItem.getFrontCard());
        assertEquals("NewBack", cardItem.getBackCard());
        verify(cardItemMapper).toCardItemResponseDto(cardItem);
        assertSame(responseDto, result);
    }

    @Test
    void deleteCardsPreservesRemainingCards() throws Exception {
        CardItemServiceImpl service = new CardItemServiceImpl(flashcardService, cardItemMapper,
                authenticationContext, cardItemRepository, assetService, flashcardRepository, flashcardPermissionService);

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setSet(set);
        flashcard.setUser(user);

        CardItem card1 = createCardItem(1L, flashcard);
        CardItem card2 = createCardItem(2L, flashcard);
        CardItem card3 = createCardItem(3L, flashcard);
        CardItem card4 = createCardItem(4L, flashcard);
        CardItem card5 = createCardItem(5L, flashcard);

        flashcard.setCards(new ArrayList<>(Arrays.asList(card1, card2, card3, card4, card5)));

        List<Long> cardIds = List.of(1L, 2L);
        List<CardItem> cardsToDelete = List.of(card1, card2);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(cardItemRepository.findAllWithImageByIdIn(cardIds)).thenReturn(cardsToDelete);

        service.deleteCards(1L, 1L, cardIds);

        verify(cardItemRepository).deleteAll(cardsToDelete);
        verify(flashcardRepository).updateUpdatedAt(eq(1L), any());
    }

    @Test
    void deleteAllCardsThrowsIfLessThanMin() throws Exception {
        CardItemServiceImpl service = new CardItemServiceImpl(flashcardService, cardItemMapper,
                authenticationContext, cardItemRepository, assetService, flashcardRepository, flashcardPermissionService);

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setSet(set);
        flashcard.setUser(user);

        CardItem cardItem = createCardItem(1L, flashcard);
        flashcard.setCards(new ArrayList<>(List.of(cardItem)));

        List<Long> cardIds = List.of(1L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(cardItemRepository.findAllWithImageByIdIn(cardIds)).thenReturn(List.of(cardItem));

        service.deleteCards(1L, 1L, cardIds);

        verify(cardItemRepository).deleteAll(List.of(cardItem));
        verify(flashcardRepository).updateUpdatedAt(eq(1L), any());
    }

    private CardItem createCardItem(Long id, Flashcard flashcard) {
        CardItem ci = new CardItem();
        ci.setId(id);
        ci.setFlashcard(flashcard);
        return ci;
    }
}
