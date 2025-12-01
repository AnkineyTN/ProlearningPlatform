package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummarizeFileRequestDTO {
    @NotNull
    private Long assetId;
    @NotBlank
    private String fileUrl;

    private String lang;

    private int limit;
}
