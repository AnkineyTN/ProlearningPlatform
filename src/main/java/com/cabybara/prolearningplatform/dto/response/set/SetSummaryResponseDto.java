package com.cabybara.prolearningplatform.dto.response.set;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetSummaryResponseDto {
    private Long id;
    private String title;
    private Privacy privacy;
}
