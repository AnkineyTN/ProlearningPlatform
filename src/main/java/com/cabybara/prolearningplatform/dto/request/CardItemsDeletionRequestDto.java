package com.cabybara.prolearningplatform.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CardItemsDeletionRequestDto {
    private List<Long> cardIds;
}
