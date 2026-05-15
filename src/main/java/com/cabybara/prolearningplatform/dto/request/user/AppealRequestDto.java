package com.cabybara.prolearningplatform.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppealRequestDto {
    @NotBlank(message = "reason is required")
    private String reason;
}
