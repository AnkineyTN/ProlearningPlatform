package com.cabybara.prolearningplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetDetailNoteResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String privacy;
    private String content;
    private List<GetDocsInNoteResponseDTO> noteDocs;
}
