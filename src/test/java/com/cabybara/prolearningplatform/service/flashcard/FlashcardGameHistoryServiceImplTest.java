package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.helper.FlashcardGameRankingEntry;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardGameResultRequest;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameHistoryResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardGameRankingEntryDto;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.flashcard.FlashcardGameHistory;
import com.cabybara.prolearningplatform.repository.FlashcardGameHistoryRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.flashcard.impl.FlashcardGameHistoryServiceImpl;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardGameHistoryServiceImplTest {

    @Mock
    private FlashcardGameHistoryRepository flashcardGameHistoryRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @Test
    void saveGameResultSavesHistoryRecord() {
        FlashcardGameHistoryServiceImpl service = new FlashcardGameHistoryServiceImpl(
                flashcardGameHistoryRepository, flashcardRepository, userRepository, authenticationContext);

        User user = new User();
        Flashcard flashcard = new Flashcard();

        FlashcardGameResultRequest request = new FlashcardGameResultRequest(10, 120, null);

        FlashcardGameHistory savedHistory = FlashcardGameHistory.builder()
                .id(1L)
                .flashcard(flashcard)
                .user(user)
                .totalCards(10)
                .durationSeconds(120)
                .build();

        ArgumentCaptor<FlashcardGameHistory> captor = ArgumentCaptor.forClass(FlashcardGameHistory.class);

        when(flashcardRepository.getReferenceById(1L)).thenReturn(flashcard);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardGameHistoryRepository.save(captor.capture())).thenReturn(savedHistory);

        FlashcardGameHistoryResponseDto result = service.saveResult(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10, result.totalCards());
        assertEquals(120, result.durationSeconds());

        FlashcardGameHistory captured = captor.getValue();
        assertEquals(10, captured.getTotalCards());
        assertEquals(120, captured.getDurationSeconds());
        assertSame(user, captured.getUser());
        assertSame(flashcard, captured.getFlashcard());

        verify(flashcardGameHistoryRepository).save(any(FlashcardGameHistory.class));
    }

    @Test
    void getRankingReturnsOrderedByScore() {
        FlashcardGameHistoryServiceImpl service = new FlashcardGameHistoryServiceImpl(
                flashcardGameHistoryRepository, flashcardRepository, userRepository, authenticationContext);

        FlashcardGameRankingEntry entry1 = mock(FlashcardGameRankingEntry.class);
        when(entry1.getUserId()).thenReturn(1L);
        when(entry1.getFirstName()).thenReturn("user1");
        when(entry1.getLastName()).thenReturn("Doe");
        when(entry1.getBestDuration()).thenReturn(100);
        when(entry1.getPlayCount()).thenReturn(5L);

        FlashcardGameRankingEntry entry2 = mock(FlashcardGameRankingEntry.class);
        when(entry2.getUserId()).thenReturn(2L);
        when(entry2.getFirstName()).thenReturn("user2");
        when(entry2.getLastName()).thenReturn("Smith");
        when(entry2.getBestDuration()).thenReturn(120);
        when(entry2.getPlayCount()).thenReturn(3L);

        when(flashcardGameHistoryRepository.findRankingByFlashcardId(1L))
                .thenReturn(List.of(entry1, entry2));

        List<FlashcardGameRankingEntryDto> result = service.getRanking(1L);

        assertEquals(2, result.size());

        FlashcardGameRankingEntryDto first = result.get(0);
        assertEquals(1, first.rank());
        assertEquals(1L, first.userId());
        assertEquals("user1", first.firstName());
        assertEquals("Doe", first.lastName());
        assertEquals(100, first.bestDuration());
        assertEquals(5L, first.playCount());

        FlashcardGameRankingEntryDto second = result.get(1);
        assertEquals(2, second.rank());
        assertEquals(2L, second.userId());
        assertEquals("user2", second.firstName());
        assertEquals("Smith", second.lastName());
        assertEquals(120, second.bestDuration());
        assertEquals(3L, second.playCount());
    }
}
