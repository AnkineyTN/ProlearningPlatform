package com.cabybara.prolearningplatform.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExplainWrongAnswerRequestDto(
        @JsonProperty("question") @NotBlank String question,
        @JsonProperty("correctAnswer") @NotBlank String correctAnswer,
        @JsonProperty("userAnswer") @NotBlank String userAnswer,
        @JsonProperty("language") String language
) {}
