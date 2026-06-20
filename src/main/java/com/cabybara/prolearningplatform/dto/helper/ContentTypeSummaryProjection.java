package com.cabybara.prolearningplatform.dto.helper;

public interface ContentTypeSummaryProjection {
    String getContentType();
    Long getTotalActiveSeconds();
    Long getSessions();
}
