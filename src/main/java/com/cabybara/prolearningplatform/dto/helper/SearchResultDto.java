package com.cabybara.prolearningplatform.dto.helper;

import lombok.Builder;

public interface SearchResultDto {
    Long getId();
    String getTitle();
    String getDescription();
    String getType();
    double getScore();
}