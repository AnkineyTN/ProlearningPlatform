package com.cabybara.prolearningplatform.service.fcm;

import com.cabybara.prolearningplatform.dto.request.user.DeviceTokenRegistrationDto;
import com.cabybara.prolearningplatform.enums.FCMPlatform;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.fcm.DeviceToken;
import com.cabybara.prolearningplatform.repository.DeviceTokenRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.fcm.impl.DeviceTokenServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceImplTest {

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void saveOrUpdateTokenUpdatesExistingTokenForSameUser() {
        DeviceTokenServiceImpl service = new DeviceTokenServiceImpl(authenticationContext, deviceTokenRepository, userRepository);
        User user = TestFixtures.user(1L);
        DeviceToken existing = TestFixtures.deviceToken(10L, user, "token-1");
        DeviceTokenRegistrationDto request = request("token-1", "device-new", FCMPlatform.ANDROID);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(deviceTokenRepository.findByToken("token-1")).thenReturn(Optional.of(existing));

        service.saveOrUpdateToken(request);

        verify(deviceTokenRepository).deleteByUserIdAndDeviceId(1L, "device-new");
        verify(deviceTokenRepository).save(existing);
        verify(userRepository, never()).getReferenceById(1L);
        assertEquals("device-new", existing.getDeviceId());
        assertNotNull(existing.getLastActiveAt());
    }

    @Test
    void saveOrUpdateTokenReassignsExistingTokenToCurrentUser() {
        DeviceTokenServiceImpl service = new DeviceTokenServiceImpl(authenticationContext, deviceTokenRepository, userRepository);
        User currentUser = TestFixtures.user(2L);
        DeviceToken existing = TestFixtures.deviceToken(10L, TestFixtures.user(1L), "token-2");
        DeviceTokenRegistrationDto request = request("token-2", null, FCMPlatform.WEB);

        when(authenticationContext.getCurrentUserId()).thenReturn(2L);
        when(deviceTokenRepository.findByToken("token-2")).thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(2L)).thenReturn(currentUser);

        service.saveOrUpdateToken(request);

        verify(deviceTokenRepository).save(existing);
        verify(userRepository).getReferenceById(2L);
        assertSame(currentUser, existing.getUser());
        assertNotNull(existing.getLastActiveAt());
    }

    @Test
    void saveOrUpdateTokenCreatesNewTokenWhenNotExisting() {
        DeviceTokenServiceImpl service = new DeviceTokenServiceImpl(authenticationContext, deviceTokenRepository, userRepository);
        User user = TestFixtures.user(3L);
        DeviceTokenRegistrationDto request = request("token-3", "device-3", FCMPlatform.IOS);
        ArgumentCaptor<DeviceToken> captor = ArgumentCaptor.forClass(DeviceToken.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(3L);
        when(deviceTokenRepository.findByToken("token-3")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(3L)).thenReturn(user);

        service.saveOrUpdateToken(request);

        verify(deviceTokenRepository).deleteByUserIdAndDeviceId(3L, "device-3");
        verify(deviceTokenRepository).save(captor.capture());
        DeviceToken saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertEquals("token-3", saved.getToken());
        assertEquals("device-3", saved.getDeviceId());
        assertEquals(FCMPlatform.IOS, saved.getPlatform());
        assertNotNull(saved.getLastActiveAt());
    }

    @Test
    void saveOrUpdateTokenSkipsDeviceDeleteWhenDeviceIdMissing() {
        DeviceTokenServiceImpl service = new DeviceTokenServiceImpl(authenticationContext, deviceTokenRepository, userRepository);
        User user = TestFixtures.user(4L);
        DeviceTokenRegistrationDto request = request("token-4", null, FCMPlatform.WEB);

        when(authenticationContext.getCurrentUserId()).thenReturn(4L);
        when(deviceTokenRepository.findByToken("token-4")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(4L)).thenReturn(user);

        service.saveOrUpdateToken(request);

        verify(deviceTokenRepository, never()).deleteByUserIdAndDeviceId(4L, null);
    }

    @Test
    void removeTokenDelegatesToRepository() {
        DeviceTokenServiceImpl service = new DeviceTokenServiceImpl(authenticationContext, deviceTokenRepository, userRepository);

        service.removeToken("token-x");

        verify(deviceTokenRepository).deleteByToken("token-x");
    }

    private DeviceTokenRegistrationDto request(String token, String deviceId, FCMPlatform platform) {
        DeviceTokenRegistrationDto request = new DeviceTokenRegistrationDto();
        request.setToken(token);
        request.setDeviceId(deviceId);
        request.setPlatform(platform);
        return request;
    }
}
