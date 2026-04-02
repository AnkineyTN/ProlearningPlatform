package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.mapper.NotificationPreferenceMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.noti.NotificationPreference;
import com.cabybara.prolearningplatform.repository.NotificationPreferenceRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationPreferenceService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {
    private final AuthenticationContext authenticationContext;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final NotificationPreferenceMapper notificationPreferenceMapper;

    @Override
    @Transactional
    public NotificationPreferenceResponseDto getCurrentUserPreference() {
        Long userId = authenticationContext.getCurrentUserId();

        NotificationPreference pref = getOrCreateByUserId(userId);

        return notificationPreferenceMapper.toResponseDto(pref);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponseDto updateCurrentUserPreference(UpdateNotificationPreferenceRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();

        NotificationPreference pref = getOrCreateByUserId(userId);
        notificationPreferenceMapper.updateFromDto(request, pref);

        notificationPreferenceRepository.save(pref);

        return notificationPreferenceMapper.toResponseDto(pref);
    }

    @Override
    @Transactional
    public NotificationPreference getOrCreateByUserId(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDueCardReminderEnabled(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .map(NotificationPreference::isDueCardReminderEnabled)
                .orElse(false);
    }

    private NotificationPreference createDefaultPreference(Long userId) {
        User userRef = userRepository.getReferenceById(userId);

        NotificationPreference pref = NotificationPreference.builder()
                .user(userRef)
                .dueCardReminderEnabled(true)
                .weeklySummaryEnabled(true)
                .weeklySummaryDay(DayOfWeek.SUNDAY.getValue())
                .build();

        return notificationPreferenceRepository.save(pref);
    }
}
