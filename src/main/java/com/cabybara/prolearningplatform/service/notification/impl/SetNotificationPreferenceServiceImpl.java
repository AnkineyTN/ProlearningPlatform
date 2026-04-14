package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateSetNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.SetNotificationPreferenceResponseDto;
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
        preferenceRepository.save(SetNotificationPreference.builder()
                .set(setRef)
                .weeklySummaryEnabled(true)
                .weeklySummaryDay(DayOfWeek.SUNDAY.getValue())
                .build());
    }

    @Override
    @Transactional
    public SetNotificationPreference getBySetId(Long setId) {
        return preferenceRepository.findBySetId(setId)
                .orElseGet(() -> {
                    Set setRef = setRepository.getReferenceById(setId);
                    return preferenceRepository.save(SetNotificationPreference.builder()
                            .set(setRef)
                            .weeklySummaryEnabled(true)
                            .weeklySummaryDay(DayOfWeek.SUNDAY.getValue())
                            .build());
                });
    }

    @Override
    @Transactional
    public SetNotificationPreferenceResponseDto getPreferenceForCurrentUser(Long setId) {
        Long userId = authenticationContext.getCurrentUserId();
        SetNotificationPreference pref = getBySetId(setId);
        validateOwnership(pref, userId);
        return toResponseDto(pref);
    }

    @Override
    @Transactional
    public SetNotificationPreferenceResponseDto updatePreference(Long setId, UpdateSetNotificationPreferenceRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        SetNotificationPreference pref = getBySetId(setId);
        validateOwnership(pref, userId);

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

    private SetNotificationPreferenceResponseDto toResponseDto(SetNotificationPreference pref) {
        return SetNotificationPreferenceResponseDto.builder()
                .weeklySummaryEnabled(pref.isWeeklySummaryEnabled())
                .weeklySummaryDay(pref.getWeeklySummaryDay())
                .build();
    }
}
