package com.cabybara.prolearningplatform.dto.request.flashcard;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFlashcardByNoteRequestDto {
    private List<Long> noteIds;

    // TODO: Request more options
}
