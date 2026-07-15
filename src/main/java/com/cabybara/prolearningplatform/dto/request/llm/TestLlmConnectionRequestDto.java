package com.cabybara.prolearningplatform.dto.request.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestLlmConnectionRequestDto {

    @NotBlank(message = "provider is required")
    private String provider;            // openai | anthropic | google | groq (validated in service)

    @NotBlank(message = "model is required")
    @Size(max = 128)
    private String model;

    @NotBlank(message = "apiKey is required")
    @Size(max = 512)
    private String apiKey;
}
