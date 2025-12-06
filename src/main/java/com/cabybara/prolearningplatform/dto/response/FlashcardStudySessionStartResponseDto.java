package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.StudyMode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlashcardStudySessionStartResponseDto {
    private Long id;
    private StudyMode studyMode;
    private String message;
    private List<CardItemResponseDto> cards;
}
