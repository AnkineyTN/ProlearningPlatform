package com.cabybara.prolearningplatform.dto.response.flashcard;

import java.time.OffsetDateTime;
import java.util.Map;

public record FlashcardGameHistoryResponseDto(
        Long id,
        Integer totalCards,
        Integer durationSeconds,
        OffsetDateTime completedAt,
        Map<Long, Integer> wrongCardCounts
) {}
