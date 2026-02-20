package com.cabybara.prolearningplatform.dto.request.user;

import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdatingRequestDto {
    private String firstName;
    private String lastName;
    private UserLanguage language;
    private UserEducation education;
    private UserHearAppFrom hearAppFrom;
}
