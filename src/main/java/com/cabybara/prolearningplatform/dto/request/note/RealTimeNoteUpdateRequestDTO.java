package com.cabybara.prolearningplatform.dto.request.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for real-time note content update via WebSocket
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeNoteUpdateRequestDTO {
    @JsonProperty("note_id")
    private Long noteId;

    @JsonProperty("set_id")
    private Long setId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("title")
    private String title;

    @JsonProperty("cursor_position")
    private Integer cursorPosition;

    @JsonProperty("timestamp")
    private Long timestamp;
}
