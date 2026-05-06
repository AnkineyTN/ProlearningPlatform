package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFlashcardByWebRequestDto {
    private List<String> urls;

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private Language language;
}
