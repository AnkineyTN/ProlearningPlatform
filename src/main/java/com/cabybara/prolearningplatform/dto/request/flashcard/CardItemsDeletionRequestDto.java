package com.cabybara.prolearningplatform.dto.request.flashcard;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CardItemsDeletionRequestDto {
    @NotNull
    private List<Long> cardIds;
}
