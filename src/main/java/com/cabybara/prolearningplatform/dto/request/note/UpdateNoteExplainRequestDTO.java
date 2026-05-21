package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNoteExplainRequestDTO {
    @NotBlank
    private String source;
    
    @NotBlank
    private String term;
    
    @NotBlank
    private String explain;
}
