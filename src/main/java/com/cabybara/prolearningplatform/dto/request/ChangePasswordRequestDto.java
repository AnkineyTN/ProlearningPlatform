package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

@Value
public class ChangePasswordRequestDto {
    @NotNull
    @Length(min = 8, max = 20)
    String oldPassword;

    @NotNull
    @Length(min = 8, max = 20)
    String newPassword;
}
