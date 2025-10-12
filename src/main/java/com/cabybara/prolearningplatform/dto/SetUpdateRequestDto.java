package com.cabybara.prolearningplatform.dto;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetUpdateRequestDto {
    private String title;
    private String description;
    private Privacy privacy;
}
