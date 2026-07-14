package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFlashcardByNoteToSetRequestDto {
    @NotNull
    private Long targetSetId;

    private List<NoteRequestDto> notes;

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private Language language;
}
