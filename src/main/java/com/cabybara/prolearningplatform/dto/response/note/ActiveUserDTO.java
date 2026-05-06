package com.cabybara.prolearningplatform.dto.response.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for tracking active users editing a note
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUserDTO {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("cursor_position")
    private Integer cursorPosition;

    @JsonProperty("last_active")
    private Long lastActive;
}
