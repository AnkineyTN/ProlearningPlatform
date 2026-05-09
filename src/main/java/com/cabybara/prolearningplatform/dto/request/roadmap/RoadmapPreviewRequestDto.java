package com.cabybara.prolearningplatform.dto.request.roadmap;

import com.cabybara.prolearningplatform.enums.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoadmapPreviewRequestDto {

    @NotBlank
    private String goal;

    private String level;

    private Language language;
}
