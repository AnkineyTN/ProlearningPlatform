package com.cabybara.prolearningplatform.service.activity;

import com.cabybara.prolearningplatform.dto.request.activity.ActivityLogRequestDto;
import com.cabybara.prolearningplatform.dto.response.activity.ActivitySummaryResponseDto;
import com.cabybara.prolearningplatform.dto.response.activity.HeatmapDayDto;
import com.cabybara.prolearningplatform.dto.response.activity.StreakResponseDto;

import java.util.List;

public interface ActivityLogService {

    void logActivity(ActivityLogRequestDto dto);

    List<HeatmapDayDto> getHeatmap(int months);

    StreakResponseDto getStreak();

    ActivitySummaryResponseDto getSummary(int days);
}
