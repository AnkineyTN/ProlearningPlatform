package com.cabybara.prolearningplatform.dto.response.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OnboardingSubmissionResponse")
public class OnboardingSubmissionResponseDto {
    private Long id;
    private Instant submittedAt;
    private Long userId;
    private String email;
    private String displayName;
    private OnboardingDataResponseDto data;
}
