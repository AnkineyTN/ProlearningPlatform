package com.cabybara.prolearningplatform.dto.response.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetSource;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class SoundResponseDto {
    private Long id;
    private String name;
    private String description;
    private String assetUrl;
    private AssetSource source;
    private Boolean isFavorite;
    private Boolean isActive;   // đang được chọn
    private Float volume;       // null nếu chưa active
}