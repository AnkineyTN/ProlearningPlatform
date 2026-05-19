package com.cabybara.prolearningplatform.dto.request.user;

import com.cabybara.prolearningplatform.enums.FCMPlatform;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceTokenRegistrationDto {
    @NotNull
    private String token;

    @NotNull
    private FCMPlatform platform;

    private String deviceId;
}
