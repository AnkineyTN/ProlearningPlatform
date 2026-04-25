package com.cabybara.prolearningplatform.dto.request.flashcard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FlashcardGameResultRequest(
        @NotNull @Min(1) Integer totalCards,
        @NotNull @Min(1) Integer durationSeconds
) {}
