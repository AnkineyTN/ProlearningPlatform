package com.cabybara.prolearningplatform.service.flashcard.impl;

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
import com.cabybara.prolearningplatform.service.flashcard.FlashcardGameHistoryService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardGameHistoryServiceImpl implements FlashcardGameHistoryService {

    private final FlashcardGameHistoryRepository flashcardGameHistoryRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    public FlashcardGameHistoryResponseDto saveResult(Long flashcardId, FlashcardGameResultRequest request) {
        Long userId = authenticationContext.getCurrentUserId();

        Flashcard flashcard = flashcardRepository.getReferenceById(flashcardId);
        User user = userRepository.getReferenceById(userId);

        FlashcardGameHistory history = FlashcardGameHistory.builder()
                .flashcard(flashcard)
                .user(user)
                .totalCards(request.totalCards())
                .durationSeconds(request.durationSeconds())
                .wrongCardCounts(request.wrongCardCounts() != null ? request.wrongCardCounts() : new HashMap<>())
                .build();

        FlashcardGameHistory saved = flashcardGameHistoryRepository.save(history);
        return toDto(saved);
    }

    @Override
    public List<FlashcardGameHistoryResponseDto> getUserHistory(Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();
        return flashcardGameHistoryRepository
                .findByFlashcardIdAndUserIdOrderByCompletedAtDesc(flashcardId, userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<FlashcardGameRankingEntryDto> getRanking(Long flashcardId) {
        List<FlashcardGameRankingEntry> entries = flashcardGameHistoryRepository.findRankingByFlashcardId(flashcardId);
        List<FlashcardGameRankingEntryDto> result = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            FlashcardGameRankingEntry e = entries.get(i);
            result.add(new FlashcardGameRankingEntryDto(
                    i + 1,
                    e.getUserId(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getBestDuration(),
                    e.getPlayCount()
            ));
        }
        return result;
    }

    private FlashcardGameHistoryResponseDto toDto(FlashcardGameHistory h) {
        return new FlashcardGameHistoryResponseDto(h.getId(), h.getTotalCards(), h.getDurationSeconds(), h.getCompletedAt(), h.getWrongCardCounts());
    }
}
