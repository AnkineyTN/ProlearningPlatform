package com.cabybara.prolearningplatform.dto.request.flashcard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
public class GenerateFlashcardByFileRequestDto {
    private List<MultipartFile> files;

    // TODO: Request more options
}
