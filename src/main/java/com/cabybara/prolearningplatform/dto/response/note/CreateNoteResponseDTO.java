package com.cabybara.prolearningplatform.dto.response.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateNoteResponseDTO {
    private Long noteId;
}
