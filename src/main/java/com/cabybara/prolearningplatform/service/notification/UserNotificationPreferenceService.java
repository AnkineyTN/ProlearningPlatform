package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateUserNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.UserNotificationPreferenceResponseDto;

public interface UserNotificationPreferenceService {

    UserNotificationPreferenceResponseDto getPreferenceForCurrentUser();

    UserNotificationPreferenceResponseDto updatePreference(UpdateUserNotificationPreferenceRequestDto request);
}
