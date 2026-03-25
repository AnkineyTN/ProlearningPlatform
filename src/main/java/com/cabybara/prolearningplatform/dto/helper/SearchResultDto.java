package com.cabybara.prolearningplatform.dto.helper;

import lombok.Builder;

public interface SearchResultDto {
    Long getId();
    Long getSetId();
    String getTitle();
    String getDescription();
    String getType();
    double getScore();
    long getUserId();
}