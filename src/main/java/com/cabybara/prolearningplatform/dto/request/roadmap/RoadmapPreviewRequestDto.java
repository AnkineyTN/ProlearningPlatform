package com.cabybara.prolearningplatform.dto.request.roadmap;

import com.cabybara.prolearningplatform.enums.AI.RoadmapLevel;
import com.cabybara.prolearningplatform.enums.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoadmapPreviewRequestDto {

    @NotBlank
    private String goal;

    private RoadmapLevel level;

    private Language language;

    private List<String> referenceLinks;
}
