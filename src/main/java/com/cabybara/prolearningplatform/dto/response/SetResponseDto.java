package com.cabybara.prolearningplatform.dto;

import com.cabybara.prolearningplatform.enums.Privacy;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
public class SetResponseDto {
    private Long id;
    private String title;
    private String description;
    private Privacy privacy;
    private Long numNotes;
}
