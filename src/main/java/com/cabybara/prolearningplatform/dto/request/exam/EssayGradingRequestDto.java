package com.cabybara.prolearningplatform.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EssayGradingRequestDto(
        @JsonProperty("attemptId") Long attemptId,
        @JsonProperty("questionId") Long questionId,
        @JsonProperty("questionContent") String questionContent,
        @JsonProperty("expectedAnswer") String expectedAnswer,
        @JsonProperty("studentAnswer") String studentAnswer,
        @JsonProperty("maxPoints") Integer maxPoints
) {}
