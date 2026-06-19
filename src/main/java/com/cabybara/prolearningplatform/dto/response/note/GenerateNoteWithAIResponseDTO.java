package com.cabybara.prolearningplatform.dto.response.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateNoteWithAIResponseDTO {
    private Long noteId;
    private String title;
    private String content;
}
