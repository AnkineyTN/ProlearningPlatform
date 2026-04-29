package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSoundRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long assetId;
}