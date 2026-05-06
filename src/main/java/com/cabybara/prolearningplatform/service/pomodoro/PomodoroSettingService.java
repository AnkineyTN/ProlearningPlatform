package com.cabybara.prolearningplatform.service.pomodoro;

import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSettingRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.PomodoroSettingResponseDto;

public interface PomodoroSettingService {
    PomodoroSettingResponseDto getSetting();
    PomodoroSettingResponseDto updateSetting(PomodoroSettingRequestDto dto);
}