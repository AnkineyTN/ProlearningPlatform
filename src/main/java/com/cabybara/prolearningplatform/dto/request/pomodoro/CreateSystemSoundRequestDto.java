package com.cabybara.prolearningplatform.dto.request.pomodoro;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private String publicId;

    @NotBlank @URL
    private String url;
}