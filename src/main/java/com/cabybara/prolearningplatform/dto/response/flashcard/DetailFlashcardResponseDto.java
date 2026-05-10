package com.cabybara.prolearningplatform.dto.response.flashcard;

import com.cabybara.prolearningplatform.dto.response.set.SetSummaryResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DetailFlashcardResponseDto extends FlashcardResponseDto {
    private SetSummaryResponseDto set;
    private List<CardItemResponseDto> cards;
}
