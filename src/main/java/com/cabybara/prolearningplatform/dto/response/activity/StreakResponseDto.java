package com.cabybara.prolearningplatform.dto.response.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreakResponseDto {
    private int currentStreak;
    private int longestStreak;
    private String lastActiveDate;
    private boolean studiedToday;
}
