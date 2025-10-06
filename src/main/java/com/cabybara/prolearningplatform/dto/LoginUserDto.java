package com.cabybara.prolearningplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {
    @NotNull
    @Length(min = 8, max = 20)
    private String password;

    @NotNull
    @Email
    private String email;
}
