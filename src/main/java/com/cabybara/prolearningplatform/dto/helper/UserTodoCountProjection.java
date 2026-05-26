package com.cabybara.prolearningplatform.dto.helper;

import com.cabybara.prolearningplatform.enums.UserLanguage;

public interface UserTodoCountProjection {
    Long getUserId();
    Long getCount();
    UserLanguage getUserLanguage();
}
