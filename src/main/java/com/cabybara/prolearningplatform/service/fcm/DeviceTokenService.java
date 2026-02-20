package com.cabybara.prolearningplatform.service.fcm;

import com.cabybara.prolearningplatform.dto.request.user.DeviceTokenRegistrationDto;

public interface DeviceTokenService {
    void saveOrUpdateToken(DeviceTokenRegistrationDto request);

    void removeToken(String token);
}
