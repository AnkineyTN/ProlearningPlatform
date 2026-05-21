package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.UserLanguage;

public interface UserDueCardProjection {
    Long getUserId();
    Long getCardId();
    UserLanguage getUserLanguage();
}
