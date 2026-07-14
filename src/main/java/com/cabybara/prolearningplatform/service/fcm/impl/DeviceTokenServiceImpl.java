package com.cabybara.prolearningplatform.service.fcm.impl;

import com.cabybara.prolearningplatform.dto.request.user.DeviceTokenRegistrationDto;
import com.cabybara.prolearningplatform.model.fcm.DeviceToken;
import com.cabybara.prolearningplatform.repository.DeviceTokenRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.fcm.DeviceTokenService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {
    private final AuthenticationContext authenticationContext;
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void saveOrUpdateToken(DeviceTokenRegistrationDto request) {
        Long userId = authenticationContext.getCurrentUserId();

        if (request.getDeviceId() != null) {
            deviceTokenRepository.deleteByUserIdAndDeviceId(userId, request.getDeviceId());
        }

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(request.getToken());

        if (existingToken.isPresent()) {
            DeviceToken token = existingToken.get();
            token.setLastActiveAt(OffsetDateTime.now());
            token.setDeviceId(request.getDeviceId());

            if (!token.getUser().getId().equals(userId)) {
                token.setUser(userRepository.getReferenceById(userId));
            }

            deviceTokenRepository.save(token);
        } else {
            DeviceToken newToken = DeviceToken.builder()
                    .user(userRepository.getReferenceById(userId))
                    .token(request.getToken())
                    .deviceId(request.getDeviceId())
                    .platform(request.getPlatform())
                    .lastActiveAt(OffsetDateTime.now())
                    .build();
            deviceTokenRepository.save(newToken);
        }
    }

    @Override
    @Transactional
    public void removeToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
