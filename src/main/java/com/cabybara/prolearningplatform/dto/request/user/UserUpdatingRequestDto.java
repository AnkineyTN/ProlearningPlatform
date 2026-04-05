package com.cabybara.prolearningplatform.dto.request.user;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatingRequestDto {
    private String firstName;
    private String lastName;

    @Email
    @Schema(example = "user@gmail.com")
    private String email;

    private UserLanguage language;
    private UserEducation education;
    private UserHearAppFrom hearAppFrom;

    @Schema(example = "FREE")
    private AccountType accountType;

    @Schema(description = "Required when changing password if the account already has a password")
    @Length(min = 8, max = 20)
    private String currentPassword;

    @Length(min = 8, max = 20)
    @Schema(description = "New password; omit to leave password unchanged")
    private String newPassword;
}
