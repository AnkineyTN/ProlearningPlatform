package com.cabybara.prolearningplatform.dto.response;

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
    private Long createdAt;
    private Long updatedAt;
}
