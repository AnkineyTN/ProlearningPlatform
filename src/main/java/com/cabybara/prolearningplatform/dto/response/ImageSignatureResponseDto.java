package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageSignatureResponseDto {
    private String signature;
    private long timestamp;
    private String apiKey;
    private String cloudName;
    private Long assetId;
    private String uploadPreset;
}