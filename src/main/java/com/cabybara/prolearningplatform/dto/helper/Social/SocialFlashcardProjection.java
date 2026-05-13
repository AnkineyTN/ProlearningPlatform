package com.cabybara.prolearningplatform.dto.helper.Social;

import java.time.LocalDateTime;

public interface SocialFlashcardProjection {
    Long getId();
    String getTitle();
    String getDescription();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Long getOwnerId();
    String getOwnerFirstName();
    String getOwnerLastName();
    Long getNumCards();
}