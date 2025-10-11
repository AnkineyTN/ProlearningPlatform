package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNoteRequestDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String privacy;
    private String description;
    @NotNull
    private Long setId;
}
