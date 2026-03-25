package com.cabybara.prolearningplatform.dto.response.set;

import com.cabybara.prolearningplatform.enums.Privacy;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class SetResponseDto {
    private Long id;
    private String title;
    private String description;
    private Privacy privacy;
    private Long numNotes;
    private Long numFlashcards;
    private Long numExams;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
