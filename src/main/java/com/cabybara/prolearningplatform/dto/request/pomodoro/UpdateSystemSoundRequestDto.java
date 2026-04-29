package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemSoundRequestDto {
    @NotBlank
    private String name;

    private String description;

    private String publicId;
    private String url;
}