package com.cabybara.prolearningplatform.dto.response.flashcard;

import java.time.OffsetDateTime;

public record FlashcardGameHistoryResponseDto(
        Long id,
        Integer totalCards,
        Integer durationSeconds,
        OffsetDateTime completedAt
) {}
