package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExplainNoteRequestDTO {
    @NotNull
    private Long noteId;

    @NotBlank
    private String queryText;

    private String lang;
}
