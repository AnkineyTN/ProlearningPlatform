package com.cabybara.prolearningplatform.dto.response.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteExplainResponseDTO {
    private Long id;
    private Long noteId;
    private String source;
    private String term;
    private String explain;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
