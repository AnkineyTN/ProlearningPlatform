package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummarizeFileRequestDTO {
    @NotNull
    private Long noteDocsId;
    @NotBlank
    private String fileUrl;
    @NotBlank
    private String extension;
}
