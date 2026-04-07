package com.cabybara.prolearningplatform.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EssayGradingResponseDto(
        @JsonProperty("attemptId") Long attemptId,
        @JsonProperty("questionId") Long questionId,
        @JsonProperty("score") Integer score,
        @JsonProperty("maxPoints") Integer maxPoints,
        @JsonProperty("feedback") String feedback
) {}
