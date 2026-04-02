package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.model.noti.NotificationPreference;

public interface NotificationPreferenceService {

    NotificationPreferenceResponseDto getCurrentUserPreference();

    NotificationPreferenceResponseDto updateCurrentUserPreference(UpdateNotificationPreferenceRequestDto request);

    NotificationPreference getOrCreateByUserId(Long userId);

    boolean isDueCardReminderEnabled(Long userId);
}