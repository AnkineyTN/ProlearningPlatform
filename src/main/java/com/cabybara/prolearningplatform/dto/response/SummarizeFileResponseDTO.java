package com.cabybara.prolearningplatform.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummarizeFileResponseDTO {
    private Long noteDocsId;
    private String summary;
}
