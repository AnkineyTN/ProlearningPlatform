package com.cabybara.prolearningplatform.dto.request.onboarding;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "OnboardingSubmissionRequest")
public class OnboardingSubmissionRequestDto {

    @NotNull
    @Schema(example = "59")
    private Long userId;

    @Valid
    @NotNull
    private DataPayload data;

    @Data
    @Schema(name = "OnboardingSubmissionData")
    public static class DataPayload {
        @NotNull
        @Schema(example = "EN")
        private UserLanguage language;

        @NotNull
        @Schema(example = "COLLEGE")
        private UserEducation education;

        @NotNull
        @Schema(example = "FACEBOOK")
        private UserHearAppFrom hearAppFrom;

        @NotNull
        @Schema(example = "FREE")
        private AccountType accountType;
    }
}
