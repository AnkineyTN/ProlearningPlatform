package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveNoteExplainRequestDTO {
    @NotNull
    private Long noteId;
    
    @NotBlank
    private String source;
    
    @NotBlank
    private String term;
    
    @NotBlank
    private String explain;
}
