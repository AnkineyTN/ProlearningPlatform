package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.cabybara.prolearningplatform.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIGenerateFlashcardByNoteRequestDto {
    private List<NoteContentDto> notes;

    @JsonProperty("free_text")
    private String freeText;

    private Language language;
}
