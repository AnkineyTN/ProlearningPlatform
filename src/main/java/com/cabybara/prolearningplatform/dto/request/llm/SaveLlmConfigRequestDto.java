package com.cabybara.prolearningplatform.dto.request.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveLlmConfigRequestDto {

    @NotBlank(message = "provider is required")
    private String provider;            // openai | anthropic | google | groq (validated in service)

    @NotBlank(message = "model is required")
    @Size(max = 128)
    private String model;

    /**
     * Raw API key. Required on create; on update it may be omitted to keep the existing key.
     * Encrypted before persisting; never echoed back.
     */
    @Size(max = 512)
    private String apiKey;

    @Size(max = 64)
    private String displayName;

    /** When true, this config becomes the active one (others are deactivated). */
    private Boolean setActive;
}
