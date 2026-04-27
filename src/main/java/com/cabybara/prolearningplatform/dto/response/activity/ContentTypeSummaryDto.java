package com.cabybara.prolearningplatform.dto.response.activity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentTypeSummaryDto {
    private String contentType;
    private long totalMinutes;
    private long sessions;
}
