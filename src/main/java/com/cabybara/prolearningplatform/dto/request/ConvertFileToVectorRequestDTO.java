package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertFileToVectorRequestDTO {
    @NotNull
    private Long noteDocsId;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
    @NotBlank
    private String extension;
    @NotNull
    private Long noteId;
}
