package com.cabybara.prolearningplatform.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPlatformStatsResponseDto {
    private long totalUsers;
    private long blockedUsers;
    private long proUsers;
    private long freeUsers;
    private long totalNotes;
    private long totalFlashcards;
    private long totalExams;
    private long totalPomodoroSessions;
    private long totalStudySessions;
}
