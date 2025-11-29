package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.AssetType;
import lombok.Builder;
import lombok.Data;

@Data
public class AssetToDeleteDto {
    private String publicId;
    private AssetType assetType;
}
