package com.cabybara.prolearningplatform.dto.response.note;

import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharedNoteResponseDto {
    private Long id;
    private String title;
    private String description;
    private Privacy privacy;
    private String created_at;
    private String updated_at;
    private Long setId;
    private NoteRole userRole;
    private Boolean isFavorited;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
}
