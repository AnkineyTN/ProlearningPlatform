package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardUpdatingRequestDto {
    private String title;
    private String description;
    private String privacy;
}
