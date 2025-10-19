package com.cabybara.prolearningplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetAllNotesResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String privacy;
    private String created_at;
    private String updated_at;
}
