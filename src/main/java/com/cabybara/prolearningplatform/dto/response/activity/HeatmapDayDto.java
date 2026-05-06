package com.cabybara.prolearningplatform.dto.response.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeatmapDayDto {
    private String date;
    private long totalMinutes;
    private long sessions;
    private Integer bestScore;
    private long totalItems;
}
