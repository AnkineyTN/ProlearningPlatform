package com.cabybara.prolearningplatform.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExplainWrongAnswerResponseDto(
        @JsonProperty("explanation") String explanation
) {}
