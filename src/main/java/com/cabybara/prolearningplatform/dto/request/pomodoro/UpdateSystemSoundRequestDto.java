package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemSoundRequestDto {
    @NotBlank
    private String name;

    private String description;

    private Long assetId;
}