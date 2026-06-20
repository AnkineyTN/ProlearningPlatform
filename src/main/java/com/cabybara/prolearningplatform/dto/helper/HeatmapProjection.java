package com.cabybara.prolearningplatform.dto.helper;

import java.time.LocalDate;

public interface HeatmapProjection {
    LocalDate getDate();
    Long getTotalActiveSeconds();
    Long getSessions();
    Long getBestScore();
    Long getTotalItems();
}
