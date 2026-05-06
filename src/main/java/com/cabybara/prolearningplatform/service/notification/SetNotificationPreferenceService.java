package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateSetNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.SetNotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;

public interface SetNotificationPreferenceService {

    void createDefaultForSet(Long setId);

    SetNotificationPreference getBySetId(Long setId);

    SetNotificationPreferenceResponseDto getPreferenceForCurrentUser(Long setId);

    SetNotificationPreferenceResponseDto updatePreference(Long setId, UpdateSetNotificationPreferenceRequestDto request);
}
