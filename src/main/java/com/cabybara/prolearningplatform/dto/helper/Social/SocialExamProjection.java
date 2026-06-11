package com.cabybara.prolearningplatform.dto.helper.Social;

import java.sql.Timestamp;

public interface SocialExamProjection {
    Long getId();
    Long getSetId();
    String getTitle();
    String getDescription();
    Long getDuration();
    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();
    Long getOwnerId();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getOwnerAvatarUrl();
    Long getNumQuestions();
}