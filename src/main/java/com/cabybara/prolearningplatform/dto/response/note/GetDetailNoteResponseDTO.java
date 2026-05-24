package com.cabybara.prolearningplatform.dto.response.note;

import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDetailNoteResponseDTO {
    private Long id;
    /** Owning set — useful for clients calling `/sets/{setId}/notes/...` routes. */
    private Long setId;
    private String title;
    private String description;
    private Privacy privacy;
    private String content;
    private NoteRole userRole;
    private List<GetDocsInNoteResponseDTO> noteDocs;
    private List<GetDocsInNoteResponseDTO> noteImgs;
}
