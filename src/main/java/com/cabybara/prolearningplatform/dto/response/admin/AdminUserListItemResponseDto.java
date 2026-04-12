package com.cabybara.prolearningplatform.dto.response.admin;

import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingDataResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
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
@Schema(description = "Admin directory row: account profile plus optional onboarding questionnaire data.")
public class AdminUserListItemResponseDto {

    @Schema(description = "User account and roles")
    private UserResponseDto user;

    @Schema(description = """
            Values submitted during onboarding (language, education, how they heard about the app, tier).
            Null or omitted when the user has not completed onboarding; clients may display an empty cell or a placeholder such as "--".""")
    private OnboardingDataResponseDto onboarding;

    @Schema(description = "Time of last profile update when onboarding is considered complete; null otherwise.")
    private Instant onboardingSubmittedAt;
}
