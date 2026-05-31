package com.cabybara.prolearningplatform.dto.response.set;

import com.cabybara.prolearningplatform.enums.Privacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetResponseDto {
    private Long id;
    private Long roadmapId;
    private String title;
    private String description;
    private Privacy privacy;
    private Long numNotes;
    private Long numFlashcards;
    private Long numExams;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
