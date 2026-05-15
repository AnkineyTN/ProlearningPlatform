package com.cabybara.prolearningplatform.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublicAppealRequestDto {
    @Email
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "reason is required")
    private String reason;
}
