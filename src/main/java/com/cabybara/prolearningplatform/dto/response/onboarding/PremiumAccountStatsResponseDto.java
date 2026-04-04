package com.cabybara.prolearningplatform.dto.response.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumAccountStatsResponseDto {
    private long proCount;
    private long freeCount;
    private double proPercent;
}
