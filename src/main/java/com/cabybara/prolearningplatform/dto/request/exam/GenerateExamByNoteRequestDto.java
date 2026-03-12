package com.cabybara.prolearningplatform.dto.request.exam;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExamByNoteRequestDto {
    private List<Long> noteIds;
    private Map<String, Integer> questions;
    private String language;
}
