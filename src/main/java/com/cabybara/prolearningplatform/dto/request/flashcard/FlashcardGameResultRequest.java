package com.cabybara.prolearningplatform.dto.request.flashcard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record FlashcardGameResultRequest(
        @NotNull @Min(1) Integer totalCards,
        @NotNull @Min(1) Integer durationSeconds,
        Map<Long, Integer> wrongCardCounts
) {}
