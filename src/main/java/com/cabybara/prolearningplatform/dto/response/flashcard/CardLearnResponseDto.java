package com.cabybara.prolearningplatform.dto.response.flashcard;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CardLearnResponseDto extends CardItemResponseDto {
    private Long flashcardId;
}
