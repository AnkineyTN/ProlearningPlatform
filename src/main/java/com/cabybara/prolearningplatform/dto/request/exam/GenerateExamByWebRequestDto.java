package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Language;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExamByWebRequestDto {
    private List<String> urls;

    private Map<String, Integer> questions;

    private Map<String, Double> difficulty;

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private Language language;
}
