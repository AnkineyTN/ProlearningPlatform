package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteNoteDocRequestDTO {
    @NotNull
    String publicId;
    @NotBlank
    String extension;
}
