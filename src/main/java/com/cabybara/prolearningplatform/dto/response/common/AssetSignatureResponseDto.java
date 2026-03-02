package com.cabybara.prolearningplatform.dto.response.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetSignatureResponseDto {
    private String signature;
    private long timestamp;
    private String apiKey;
    private String cloudName;
    private Long assetId;
    private String uploadPreset;
    private String uploadResourceType;
}