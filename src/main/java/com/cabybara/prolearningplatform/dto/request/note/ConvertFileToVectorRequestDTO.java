package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertFileToVectorRequestDTO {
    @NotNull
    private Long noteId;
    @NotNull
    private Long assetId;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
}
