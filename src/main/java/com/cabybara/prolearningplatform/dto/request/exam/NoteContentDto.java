package com.cabybara.prolearningplatform.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
