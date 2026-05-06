package com.cabybara.prolearningplatform.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class GenerateExamByFileRequestDto {
    private List<MultipartFile> files;

    private String questions;

    private String difficulty;

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private String language;
}
