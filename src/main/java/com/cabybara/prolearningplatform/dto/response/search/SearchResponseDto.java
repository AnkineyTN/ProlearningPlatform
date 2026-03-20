package com.cabybara.prolearningplatform.dto.response.search;

public record SearchResponseDto(
        Long id,
        String title,
        String description,
        String type
) {
}
