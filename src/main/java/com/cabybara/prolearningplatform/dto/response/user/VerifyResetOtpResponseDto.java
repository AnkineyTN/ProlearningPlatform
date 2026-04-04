package com.cabybara.prolearningplatform.dto.response.user;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResetOtpResponseDto {
    private String resetToken;
}
