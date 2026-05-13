package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIGenerateExamByNoteRequestDto {
    private List<NoteContentDto> notes;

    private Map<String, Integer> questions;

    private Map<String, Double> difficulty;

    @JsonProperty("free_text")
    private String freeText;

    private Language language;
}
