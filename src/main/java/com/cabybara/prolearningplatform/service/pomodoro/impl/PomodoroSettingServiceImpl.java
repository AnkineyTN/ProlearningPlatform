package com.cabybara.prolearningplatform.service.pomodoro.impl;

import org.springframework.stereotype.Service;

import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSettingRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.PomodoroSettingResponseDto;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSetting;
import com.cabybara.prolearningplatform.repository.PomodoroSettingRepository;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSettingService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSettingServiceImpl implements PomodoroSettingService {
    private final PomodoroSettingRepository settingRepository;
    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    @Override
    public PomodoroSettingResponseDto getSetting() {
        Long userId = authenticationContext.getCurrentUserId();
        PomodoroSetting setting = settingRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSetting(userId));
        return toDto(setting);
    }

    @Override
    public PomodoroSettingResponseDto updateSetting(PomodoroSettingRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();
        PomodoroSetting setting = settingRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSetting(userId));

        setting.setPomodoroDuration(dto.getPomodoroDuration());
        setting.setShortBreak(dto.getShortBreak());
        setting.setLongBreak(dto.getLongBreak());
        setting.setLongBreakInterval(dto.getLongBreakInterval());
        setting.setAutoStartBreak(dto.getAutoStartBreak());
        setting.setAutoStartPomodoro(dto.getAutoStartPomodoro());

        return toDto(settingRepository.save(setting));
    }

    private PomodoroSetting createDefaultSetting(Long userId) {
        User user = userService.getUserById(userId);
        PomodoroSetting s = PomodoroSetting.builder().user(user).build();
        return settingRepository.save(s);
    }

    private PomodoroSettingResponseDto toDto(PomodoroSetting s) {
        return PomodoroSettingResponseDto.builder()
                .pomodoroDuration(s.getPomodoroDuration())
                .shortBreak(s.getShortBreak())
                .longBreak(s.getLongBreak())
                .longBreakInterval(s.getLongBreakInterval())
                .autoStartBreak(s.getAutoStartBreak())
                .autoStartPomodoro(s.getAutoStartPomodoro())
                .build();
    }
}
