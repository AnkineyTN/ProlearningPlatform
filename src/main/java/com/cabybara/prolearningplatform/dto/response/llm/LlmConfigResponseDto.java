package com.cabybara.prolearningplatform.dto.response.llm;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class LlmConfigResponseDto {
    private Long id;
    private String displayName;
    private String provider;        // wire value, e.g. "openai"
    private String model;
    private String apiKeyMasked;    // e.g. "****1234"; never the raw key
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
