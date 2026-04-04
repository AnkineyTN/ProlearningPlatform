package com.cabybara.prolearningplatform.dto.request.email;

import com.google.auto.value.AutoValue.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Builder
public class VerifyOtpRequest {
    @NotNull
    @NotEmpty
    @NotBlank
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    private String otp;
}
