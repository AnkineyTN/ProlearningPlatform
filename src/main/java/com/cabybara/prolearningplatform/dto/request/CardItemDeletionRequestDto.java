package com.cabybara.prolearningplatform.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CardItemDeletionRequestDto {
    private List<Long> cardIds;
}
