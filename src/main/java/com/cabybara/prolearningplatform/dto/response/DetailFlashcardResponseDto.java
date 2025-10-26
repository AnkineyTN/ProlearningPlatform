package com.cabybara.prolearningplatform.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DetailFlashcardResponseDto extends FlashcardResponseDto {
    private List<CardItemResponseDto> cards;
}
