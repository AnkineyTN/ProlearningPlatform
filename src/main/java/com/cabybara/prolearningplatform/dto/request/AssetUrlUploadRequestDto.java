package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetUrlUploadRequestDto {
    @NotBlank
    private String sourceUrl;

    @NotBlank
    private AssetType assetType;
}
