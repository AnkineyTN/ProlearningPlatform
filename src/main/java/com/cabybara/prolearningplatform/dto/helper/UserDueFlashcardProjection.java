package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.UserLanguage;

public interface UserDueFlashcardProjection {
    Long getUserId();
    Long getSetId();
    Long getFlashcardId();
    String getFlashcardTitle();
    Long getDueCount();
    UserLanguage getUserLanguage();
}
