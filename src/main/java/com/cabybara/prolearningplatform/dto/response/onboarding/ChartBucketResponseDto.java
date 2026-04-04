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
public class ChartBucketResponseDto {
    @Schema(example = "COLLEGE")
    private String label;
    private long count;
    private double percent;
}
