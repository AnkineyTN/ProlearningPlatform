package com.cabybara.prolearningplatform.dto.request.note;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExplainNoteRequestDTO {
    @NotNull
    @JsonProperty("note_id")
    @JsonAlias("noteId")
    private Long noteId;

    @NotBlank
    @JsonProperty("query_text")
    @JsonAlias("queryText")
    private String queryText;

    private Language language;
}
