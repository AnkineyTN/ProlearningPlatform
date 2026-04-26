package com.cabybara.prolearningplatform.dto.response.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.enums.AssetType;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class SpaceResponseDto {
    private Long id;
    private String name;
    private String description;
    private String assetUrl;
    private AssetType assetType;
    private AssetSource source;
    private Boolean isFavorite;
    private Boolean isActive;   // đang được chọn bởi user này
}