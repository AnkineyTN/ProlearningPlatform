package com.cabybara.prolearningplatform.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminBlockUserRequestDto {
    @NotBlank(message = "reason is required")
    private String reason;
}
