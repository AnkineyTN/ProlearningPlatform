package com.cabybara.prolearningplatform.dto.response.note;

import com.cabybara.prolearningplatform.enums.Privacy;
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
    private Privacy privacy;
    private String content;
    private List<GetDocsInNoteResponseDTO> noteDocs;
}
