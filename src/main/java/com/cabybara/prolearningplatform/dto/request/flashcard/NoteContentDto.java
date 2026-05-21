package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteContentDto {
    @JsonProperty("note_id")
    private Long noteId;

    private String content;

    @JsonProperty("document_urls")
    private List<String> documentUrls;
}
