package com.cabybara.prolearningplatform.dto.response.search;

public record SearchResponseDto(
        Long id,
        Long setId,
        String title,
        String description,
        String type,
        Long userId,
        Boolean isFavorited
) {
}
