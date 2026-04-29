package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSettingRequestDto {
    @NotNull 
    @Min(60) 
    @Max(7200)
    private Integer pomodoroDuration;

    @NotNull 
    @Min(60) 
    @Max(3600)
    private Integer shortBreak;

    @NotNull 
    @Min(60) 
    @Max(3600)
    private Integer longBreak;

    @NotNull 
    @Min(1) 
    @Max(10)
    private Integer longBreakInterval;

    @NotNull
    private Boolean autoStartBreak;

    @NotNull
    private Boolean autoStartPomodoro;
}