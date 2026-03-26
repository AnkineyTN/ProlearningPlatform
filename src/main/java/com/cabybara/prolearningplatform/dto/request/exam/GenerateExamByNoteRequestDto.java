package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExamByNoteRequestDto {
    private List<Long> noteIds;

    private Map<String, Integer> questions;

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private Language language;
}
