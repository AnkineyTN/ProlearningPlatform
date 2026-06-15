package com.cabybara.prolearningplatform.dto.request.note;

import com.cabybara.prolearningplatform.enums.Language;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GenerateNoteWithAIRequestDTO {

    @NotBlank
    @JsonProperty("topic")
    private String topic;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reference_links")
    private List<String> referenceLinks;

    private Language language;

    private Privacy privacy;
}
