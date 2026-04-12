package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;

public interface SetNotificationPreferenceService {

    void createDefaultForSet(Long setId);

    SetNotificationPreference getBySetId(Long setId);

    NotificationPreferenceResponseDto getPreferenceForCurrentUser(Long setId);

    NotificationPreferenceResponseDto updatePreference(Long setId, UpdateNotificationPreferenceRequestDto request);
}
