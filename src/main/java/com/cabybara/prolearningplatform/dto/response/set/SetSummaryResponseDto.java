package com.cabybara.prolearningplatform.dto.response.set;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetSummaryResponseDto {
    private Long id;
    private String title;
    private Privacy privacy;
}
