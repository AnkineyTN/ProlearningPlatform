package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.AssetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetToDeleteDto {
    private String publicId;
    private AssetType assetType;
}
