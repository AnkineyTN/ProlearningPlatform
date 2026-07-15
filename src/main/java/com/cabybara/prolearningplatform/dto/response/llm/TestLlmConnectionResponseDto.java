package com.cabybara.prolearningplatform.dto.response.llm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestLlmConnectionResponseDto {
    private boolean valid;
    private String provider;
    private String model;
    private String message;
}
