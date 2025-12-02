package com.cabybara.prolearningplatform.service.fcm;

import com.cabybara.prolearningplatform.dto.request.DeviceTokenRegistrationDto;

public interface DeviceTokenService {
    void saveOrUpdateToken(DeviceTokenRegistrationDto request);

    void removeToken(String token);
}
