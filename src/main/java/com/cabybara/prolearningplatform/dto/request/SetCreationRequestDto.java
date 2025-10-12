package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.Privacy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetCreationRequestDto {
    private String title;
    private String description;
    private Privacy privacy;
}

