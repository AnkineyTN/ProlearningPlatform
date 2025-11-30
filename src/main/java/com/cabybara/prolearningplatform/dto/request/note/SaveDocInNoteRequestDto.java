package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveDocInNoteRequestDto {
    @NotNull
    private Long noteId;

    @NotNull
    private Long assetId;
}
