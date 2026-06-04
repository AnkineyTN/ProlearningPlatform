package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateUserNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.UserNotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.noti.UserNotificationPreference;
import com.cabybara.prolearningplatform.repository.UserNotificationPreferenceRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.notification.UserNotificationPreferenceService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotificationPreferenceServiceImpl implements UserNotificationPreferenceService {

    private final UserNotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public UserNotificationPreferenceResponseDto getPreferenceForCurrentUser() {
        Long userId = authenticationContext.getCurrentUserId();
        UserNotificationPreference pref = getOrCreate(userId);
        return toResponseDto(pref);
    }

    @Override
    @Transactional
    public UserNotificationPreferenceResponseDto updatePreference(UpdateUserNotificationPreferenceRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        UserNotificationPreference pref = getOrCreate(userId);

        if (request.getDueCardReminderEnabled() != null) {
            pref.setDueCardReminderEnabled(request.getDueCardReminderEnabled());
        }
        if (request.getSystemAnnouncementEnabled() != null) {
            pref.setSystemAnnouncementEnabled(request.getSystemAnnouncementEnabled());
        }
        if (request.getAccountActivityEnabled() != null) {
            pref.setAccountActivityEnabled(request.getAccountActivityEnabled());
        }
        if (request.getDailyTodoReminderEnabled() != null) {
            pref.setDailyTodoReminderEnabled(request.getDailyTodoReminderEnabled());
        }
        if (request.getDailyTodoReminderHour() != null) {
            pref.setDailyTodoReminderHour(request.getDailyTodoReminderHour());
        }
        if (request.getWeeklyTodoReminderEnabled() != null) {
            pref.setWeeklyTodoReminderEnabled(request.getWeeklyTodoReminderEnabled());
        }
        if (request.getWeeklyTodoReminderHour() != null) {
            pref.setWeeklyTodoReminderHour(request.getWeeklyTodoReminderHour());
        }
        if (request.getGoalDeadlineReminderEnabled() != null) {
            pref.setGoalDeadlineReminderEnabled(request.getGoalDeadlineReminderEnabled());
        }
        if (request.getGoalInactiveReminderEnabled() != null) {
            pref.setGoalInactiveReminderEnabled(request.getGoalInactiveReminderEnabled());
        }
        if (request.getGoalReminderHour() != null) {
            pref.setGoalReminderHour(request.getGoalReminderHour());
        }

        preferenceRepository.save(pref);
        return toResponseDto(pref);
    }

    private UserNotificationPreference getOrCreate(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User userRef = userRepository.getReferenceById(userId);
                    return preferenceRepository.save(
                            UserNotificationPreference.builder().user(userRef).build()
                    );
                });
    }

    private UserNotificationPreferenceResponseDto toResponseDto(UserNotificationPreference pref) {
        return UserNotificationPreferenceResponseDto.builder()
                .dueCardReminderEnabled(pref.isDueCardReminderEnabled())
                .systemAnnouncementEnabled(pref.isSystemAnnouncementEnabled())
                .accountActivityEnabled(pref.isAccountActivityEnabled())
                .dailyTodoReminderEnabled(pref.isDailyTodoReminderEnabled())
                .dailyTodoReminderHour(pref.getDailyTodoReminderHour())
                .weeklyTodoReminderEnabled(pref.isWeeklyTodoReminderEnabled())
                .weeklyTodoReminderHour(pref.getWeeklyTodoReminderHour())
                .goalDeadlineReminderEnabled(pref.isGoalDeadlineReminderEnabled())
                .goalInactiveReminderEnabled(pref.isGoalInactiveReminderEnabled())
                .goalReminderHour(pref.getGoalReminderHour())
                .build();
    }
}
