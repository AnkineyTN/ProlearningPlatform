package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlashcardStudySessionStartResponseDto {
    private Long id;
    private List<CardItemResponseDto> cards;
}
