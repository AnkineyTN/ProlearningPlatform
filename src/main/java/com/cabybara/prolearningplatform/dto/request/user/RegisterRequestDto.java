package com.cabybara.prolearningplatform.dto.request.user;

import com.cabybara.prolearningplatform.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotNull
    @Pattern(regexp = "^[\\p{L}\\p{Nd}\\s]+$", message = "Not contains special characters")
    private String firstName;

    @NotNull
    @Pattern(regexp = "^[\\p{L}\\p{Nd}\\s]+$", message = "Not contains special characters")
    private String lastName;

    @NotNull
    @Length(min = 6, max = 30)
    @Schema(minLength = 6, maxLength = 30)
    private String password;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Schema(implementation = Role.class)
    private String role;
}

