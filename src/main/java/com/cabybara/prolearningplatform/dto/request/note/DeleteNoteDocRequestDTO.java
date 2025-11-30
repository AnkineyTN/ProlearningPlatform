package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeleteNoteDocRequestDTO {
    @NotNull
    Long noteId;
    @NotNull
    Long assetId;
}
