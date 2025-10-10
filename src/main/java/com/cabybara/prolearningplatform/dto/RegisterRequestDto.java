package com.cabybara.prolearningplatform.dto;

import com.cabybara.prolearningplatform.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Length(min = 8, max = 20)
    @Schema(minLength = 8, maxLength = 20)
    private String password;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Schema(implementation = Role.class)
    private String role;
}

