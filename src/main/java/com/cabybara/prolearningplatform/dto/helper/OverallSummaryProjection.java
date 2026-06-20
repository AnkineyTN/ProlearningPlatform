package com.cabybara.prolearningplatform.dto.helper;

public interface OverallSummaryProjection {
    Long getTotalActiveSeconds();
    Long getTotalSessions();
    Long getTotalItems();
    Double getAvgScore();
}
