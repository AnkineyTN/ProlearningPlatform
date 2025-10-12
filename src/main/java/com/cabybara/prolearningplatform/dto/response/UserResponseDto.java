package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<Role> roles;
    private UserLanguage language;
    private UserEducation education;
    private UserHearAppFrom hearAppFrom;
}
