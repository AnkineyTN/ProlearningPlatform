package com.cabybara.prolearningplatform.dto.request.pomodoro;

import java.time.OffsetDateTime;

import com.cabybara.prolearningplatform.enums.PomodoroSessionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogSessionRequestDto {
    @NotNull
    private PomodoroSessionType type;

    @NotNull 
    @Min(1)
    private Integer duration;    // giây thực tế

    @NotNull 
    @Min(1)
    private Integer planned;     // giây dự kiến

    @NotNull
    private Boolean completed;

    @NotNull
    private OffsetDateTime startedAt;

    @NotNull
    private OffsetDateTime endedAt;
}
