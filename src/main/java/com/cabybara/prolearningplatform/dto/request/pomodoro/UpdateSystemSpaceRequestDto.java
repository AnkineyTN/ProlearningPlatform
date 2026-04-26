package com.cabybara.prolearningplatform.dto.request.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemSpaceRequestDto {
    @NotBlank
    private String name;

    private String description;

    private String publicId;
    private String url;
    private AssetType assetType;
}