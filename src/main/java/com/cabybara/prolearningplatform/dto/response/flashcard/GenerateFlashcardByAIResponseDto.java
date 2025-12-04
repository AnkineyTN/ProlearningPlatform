package com.cabybara.prolearningplatform.dto.response.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GenerateFlashcardByAIResponseDto {
    private String content;
}
