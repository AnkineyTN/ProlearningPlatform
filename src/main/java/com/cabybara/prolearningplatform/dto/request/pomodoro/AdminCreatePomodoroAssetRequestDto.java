package com.cabybara.prolearningplatform.dto.request.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCreatePomodoroAssetRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String url;

    @NotNull
    private AssetType assetType;
}
