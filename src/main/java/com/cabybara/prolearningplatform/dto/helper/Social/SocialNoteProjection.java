package com.cabybara.prolearningplatform.dto.helper.Social;

import java.sql.Timestamp;

public interface SocialNoteProjection {
    Long getId();
    String getTitle();
    String getDescription();
    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();
    Long getOwnerId();
    String getOwnerFirstName();
    String getOwnerLastName();
}