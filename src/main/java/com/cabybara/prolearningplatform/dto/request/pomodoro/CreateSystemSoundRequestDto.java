package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSystemSoundRequestDto {
@NotBlank
    private String name;

    private String description;

    @NotNull
    private Long assetId;
}