package com.cabybara.prolearningplatform.dto.helper.Social;

import java.sql.Timestamp;

public interface SocialFlashcardProjection {
    Long getId();
    Long getSetId();
    String getTitle();
    String getDescription();
    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();
    Long getOwnerId();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getOwnerAvatarUrl();
    Long getNumCards();
}