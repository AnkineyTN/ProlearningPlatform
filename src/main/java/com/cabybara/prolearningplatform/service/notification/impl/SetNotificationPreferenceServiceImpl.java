package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;
import com.cabybara.prolearningplatform.repository.SetNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.notification.SetNotificationPreferenceService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

@Service
@RequiredArgsConstructor
public class SetNotificationPreferenceServiceImpl implements SetNotificationPreferenceService {

    private final SetNotificationPreferenceRepository preferenceRepository;
    private final SetRepository setRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public void createDefaultForSet(Long setId) {
        Set setRef = setRepository.getReferenceById(setId);
        SetNotificationPreference pref = SetNotificationPreference.builder()
                .set(setRef)
                .dueCardReminderEnabled(true)
                .weeklySummaryEnabled(true)
                .weeklySummaryDay(DayOfWeek.SUNDAY.getValue())
                .build();
        preferenceRepository.save(pref);
    }

    @Override
    @Transactional
    public SetNotificationPreference getBySetId(Long setId) {
        return preferenceRepository.findBySetId(setId)
                .orElseGet(() -> {
                    createDefaultForSet(setId);
                    return preferenceRepository.findBySetId(setId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Notification preference not found for set: " + setId));
                });
    }

    @Override
    @Transactional
    public NotificationPreferenceResponseDto getPreferenceForCurrentUser(Long setId) {
        Long userId = authenticationContext.getCurrentUserId();
        SetNotificationPreference pref = getBySetId(setId);
        validateOwnership(pref, userId);
        return toResponseDto(pref);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponseDto updatePreference(Long setId, UpdateNotificationPreferenceRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        SetNotificationPreference pref = getBySetId(setId);
        validateOwnership(pref, userId);

        if (request.getDueCardReminderEnabled() != null) {
            pref.setDueCardReminderEnabled(request.getDueCardReminderEnabled());
        }
        if (request.getWeeklySummaryEnabled() != null) {
            pref.setWeeklySummaryEnabled(request.getWeeklySummaryEnabled());
        }
        if (request.getWeeklySummaryDay() != null) {
            pref.setWeeklySummaryDay(request.getWeeklySummaryDay());
        }

        preferenceRepository.save(pref);
        return toResponseDto(pref);
    }

    private void validateOwnership(SetNotificationPreference pref, Long userId) {
        if (!pref.getSet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have access to this set's notification preferences");
        }
    }

    private NotificationPreferenceResponseDto toResponseDto(SetNotificationPreference pref) {
        return NotificationPreferenceResponseDto.builder()
                .dueCardReminderEnabled(pref.isDueCardReminderEnabled())
                .weeklySummaryEnabled(pref.isWeeklySummaryEnabled())
                .weeklySummaryDay(pref.getWeeklySummaryDay())
                .build();
    }
}
