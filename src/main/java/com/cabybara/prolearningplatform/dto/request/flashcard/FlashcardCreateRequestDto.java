package com.cabybara.prolearningplatform.dto.request.flashcard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class FlashcardCreateRequestDto {
    @NotNull
    private String title;

    private String description;

    @NotNull
    private String privacy;

    @Valid
    private List<CardItemCreateRequestDto> cards;
}
