package com.cabybara.prolearningplatform.dto.response.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO for sending list of active users editing a note
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUsersResponseDTO {
    @JsonProperty("note_id")
    private Long noteId;

    @JsonProperty("active_users")
    private List<ActiveUserDTO> activeUsers;

    @JsonProperty("count")
    private Integer count;
}
