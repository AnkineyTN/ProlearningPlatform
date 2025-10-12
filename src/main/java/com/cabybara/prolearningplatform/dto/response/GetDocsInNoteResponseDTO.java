package com.cabybara.prolearningplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetDocsInNoteResponseDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String extension;
    private String publicId;
}
