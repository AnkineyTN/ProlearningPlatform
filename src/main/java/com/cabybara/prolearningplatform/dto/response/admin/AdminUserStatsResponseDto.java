package com.cabybara.prolearningplatform.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AdminUserStatsResponseDto {
    private long noteCount;
    private long flashcardCount;
    private long examCount;
    private long pomodoroSessionCount;
    private OffsetDateTime registeredAt;
}
