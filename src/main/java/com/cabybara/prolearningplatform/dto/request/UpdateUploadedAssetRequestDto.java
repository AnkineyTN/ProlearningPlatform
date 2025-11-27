package com.cabybara.prolearningplatform.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUploadedAssetRequestDto {
    private Long assetId;
    private String publicId;
    private String url;
}
