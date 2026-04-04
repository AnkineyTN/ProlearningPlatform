package com.cabybara.prolearningplatform.dto.response.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OnboardingAnalyticsResponse")
public class OnboardingAnalyticsResponseDto {
    private long totalRegisteredUsers;
    private List<ChartBucketResponseDto> education;
    private List<ChartBucketResponseDto> hearAppFrom;
    private PremiumAccountStatsResponseDto premium;
}
