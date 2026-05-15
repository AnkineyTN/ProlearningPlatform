package com.cabybara.prolearningplatform.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetAvatarRequestDto {
    @NotNull(message = "assetId is required")
    private Long assetId;
}
