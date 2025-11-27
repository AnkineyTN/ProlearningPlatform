package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUploadedImageRequestDto {
    @NotNull
    private Long assetId;
    @NotNull
    private String publicId;
    @NotNull
    private String url;
}
