package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.UserLanguage;

public interface UserDueStatDto {
    Long getUserId();
    Long getDueCount();
    UserLanguage getUserLanguage();
}