package com.cabybara.prolearningplatform.dto.response.activity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActivitySummaryResponseDto {
    private long totalMinutes;
    private long totalSessions;
    private long totalItems;
    private Double avgExamScore;
    private List<ContentTypeSummaryDto> breakdown;
}
