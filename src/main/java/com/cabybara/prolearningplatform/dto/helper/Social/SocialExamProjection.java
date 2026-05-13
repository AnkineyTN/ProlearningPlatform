package com.cabybara.prolearningplatform.dto.helper.Social;

import java.time.LocalDateTime;

public interface SocialExamProjection {
    Long getId();
    String getTitle();
    String getDescription();
    Long getDuration();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Long getOwnerId();
    String getOwnerFirstName();
    String getOwnerLastName();
    Long getNumQuestions();
}