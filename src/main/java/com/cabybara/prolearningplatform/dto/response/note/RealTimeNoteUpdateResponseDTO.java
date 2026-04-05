package com.cabybara.prolearningplatform.dto.response.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for real-time note update broadcast to all connected users
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeNoteUpdateResponseDTO {
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

    @JsonProperty("updated_by_user_id")
    private Long updatedByUserId;

    @JsonProperty("updated_by_username")
    private String updatedByUsername;

    @JsonProperty("timestamp")
    private Long timestamp;
}
