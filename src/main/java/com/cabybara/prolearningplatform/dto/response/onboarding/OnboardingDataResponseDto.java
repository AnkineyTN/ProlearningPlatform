package com.cabybara.prolearningplatform.dto.response.onboarding;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OnboardingDataResponse")
public class OnboardingDataResponseDto {
    private UserLanguage language;
    private UserEducation education;
    private UserHearAppFrom hearAppFrom;
    private AccountType accountType;
}
