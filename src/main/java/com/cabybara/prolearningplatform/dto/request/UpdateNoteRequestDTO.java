package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNoteRequestDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String privacy;
    private String description;
}
