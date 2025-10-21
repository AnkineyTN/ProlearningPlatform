package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNoteRequestDTO {
    @NotBlank
    private String title;
    private Privacy privacy;
    private String description;
    @NotNull
    private Long setId;
}
