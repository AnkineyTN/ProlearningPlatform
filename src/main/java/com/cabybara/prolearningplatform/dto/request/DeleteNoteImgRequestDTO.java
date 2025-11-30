package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeleteNoteImgRequestDTO {
    @NotNull
    private Long noteId;
    @NotBlank
    private String fileUrl;
}
