package com.cabybara.prolearningplatform.dto.response.flashcard;

public record FlashcardGameRankingEntryDto(
        int rank,
        Long userId,
        String firstName,
        String lastName,
        Integer bestDuration,
        Long playCount
) {}
