package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveNoteRequestDTO {
    @NotBlank
    private String title;
    private String content;
}
