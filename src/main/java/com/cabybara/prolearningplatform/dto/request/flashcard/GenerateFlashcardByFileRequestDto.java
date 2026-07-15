package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("free_text")
    @JsonAlias("freeText")
    private String freeText;

    private String language;
}
