package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class FlashcardCreateRequestDto {
    @NotNull
    private String title;

    private String description;

    @NotNull
    private String privacy;

    @Valid
    private List<CardItemCreateRequestDto> cards;
}
