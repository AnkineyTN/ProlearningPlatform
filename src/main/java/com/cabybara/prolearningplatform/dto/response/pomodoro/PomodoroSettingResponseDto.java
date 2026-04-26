package com.cabybara.prolearningplatform.dto.response.pomodoro;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class PomodoroSettingResponseDto {
    private Integer pomodoroDuration;
    private Integer shortBreak;
    private Integer longBreak;
    private Integer longBreakInterval;
    private Boolean autoStartBreak;
    private Boolean autoStartPomodoro;
}
