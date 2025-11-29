package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetUrlUploadResponseDto {
    private Long assetId;
    private String finalUrl;
}